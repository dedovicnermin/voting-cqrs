package io.voting.streams.voteintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.voteintegrity.config.Constants;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.topology.aggregate.VoteAggregator;
import io.voting.streams.voteintegrity.topology.mappers.CloudEventMapper;
import io.voting.streams.voteintegrity.topology.mappers.ElectionIdExtractor;
import io.voting.streams.voteintegrity.topology.predicates.FirstVoteProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.Properties;

@Slf4j
public final class VoteIntegrityTopology {

  private static final Aggregator<String, ElectionVote, ElectionSummary> aggregator = new VoteAggregator();
  private static final Predicate<String, ElectionSummary> firstVoteProcessor = new FirstVoteProcessor();
  private static final KeyValueMapper<String, ElectionSummary, String> electionIdExtractor = new ElectionIdExtractor();
  private static final ValueMapper<ElectionSummary, CloudEvent> cloudEventMapper = new CloudEventMapper();

  private VoteIntegrityTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final String inputTopic = properties.getProperty(Constants.INPUT_TOPIC_CONFIG);
    final String outputTopic = properties.getProperty(Constants.OUTPUT_TOPIC_CONFIG);
    log.debug("VoteIntegrity topology inputTopic / outputTopic : {} / {}", inputTopic, outputTopic);

    builder
            .stream(inputTopic, Consumed.with(Serdes.String(), StreamUtils.getJsonSerde(ElectionVote.class)))
            .groupByKey()
            .aggregate(
                    VoteAggregator.initializer(),
                    aggregator,
                    VoteAggregator.materialize()
            )
            .toStream()
            .filter(firstVoteProcessor)
            .selectKey(electionIdExtractor)
            .mapValues(cloudEventMapper)
            .to(outputTopic, Produced.with(Serdes.String(), StreamUtils.getCESerde()));
    return builder.build();
  }

}
