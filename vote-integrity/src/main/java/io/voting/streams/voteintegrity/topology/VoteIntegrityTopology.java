package io.voting.streams.voteintegrity.topology;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.voteintegrity.config.Constants;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import io.voting.common.library.models.ElectionVote;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.net.URI;
import java.util.Properties;
import java.util.UUID;

public final class VoteIntegrityTopology {

  private VoteIntegrityTopology() {}

  private static final CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://" + VoteIntegrityTopology.class.getSimpleName()))
          .withType(CloudEventTypes.ELECTION_VOTE_EVENT);


  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties, final ObjectMapper objectMapper) {
    final KStream<String, ElectionVote> input = createVoteEventSource(builder, properties.getProperty(Constants.INPUT_TOPIC_CONFIG));
    final KStream<String, ElectionSummary> aggregate = aggregateVoteStream(input);
    final KStream<String, ElectionSummary> validVotes = filterValidVotesAndReKey(aggregate);
    mapAndSend(validVotes, objectMapper, properties.getProperty(Constants.OUTPUT_TOPIC_CONFIG));
    return builder.build();
  }


  public static KStream<String, ElectionVote> createVoteEventSource(final StreamsBuilder builder, final String inputTopic) {
    return builder.stream(inputTopic, Consumed.with(Serdes.String(), StreamUtils.getJsonSerde(ElectionVote.class)));
  }

  public static KStream<String, ElectionSummary> aggregateVoteStream(final KStream<String, ElectionVote> voteKStream) {
    return voteKStream
            .groupByKey()
            .aggregate(
                    ElectionSummary::new,
                    (s, electionVote, electionSummary) -> electionSummary.add(electionVote),
                    Materialized.<String, ElectionSummary, KeyValueStore<Bytes, byte[]>>as("votes.integrity.aggregate")
                            .withKeySerde(Serdes.String())
                            .withValueSerde(StreamUtils.getJsonSerde(ElectionSummary.class))
            ).toStream();
  }

  public static KStream<String, ElectionSummary> filterValidVotesAndReKey(final KStream<String, ElectionSummary> summaryTable) {
    return summaryTable
            .filter((s, electionSummary) -> electionSummary.getVoteAttempts() == 1L)
            .selectKey((s, electionSummary) -> s.split(":")[1]);
  }

  public static void mapAndSend(final KStream<String, ElectionSummary> validVotes, final ObjectMapper mapper, final String outputTopic) {
    validVotes
            .mapValues(ElectionSummary::getVote)
            .mapValues(
                    electionVote -> ceBuilder
                            .withId(UUID.randomUUID().toString())
                            .withSubject(electionVote.getElectionId())
                            .withData(PojoCloudEventData.wrap(electionVote, mapper::writeValueAsBytes))
                            .build()
            ).to(
                    outputTopic,
                    Produced.with(Serdes.String(), StreamUtils.getCESerde())
            );
  }

}
