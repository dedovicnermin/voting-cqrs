package io.voting.streams.voteintegrity.topology.aggregate;

import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

@Slf4j
public class VoteAggregator implements Aggregator<String, ElectionVote, ElectionSummary> {

  public static Initializer<ElectionSummary> initializer() {
    return ElectionSummary::new;
  }

  @Override
  public ElectionSummary apply(final String key, final ElectionVote electionVote, final ElectionSummary electionSummary) {
    log.trace("Applying aggregate using key ({}), vote ({}) and current election summary ({})", key, electionVote, electionSummary);
    electionSummary.add(key, electionVote);
    log.trace("Election summary post-aggregate result : {}", electionSummary);
    return electionSummary;
  }

  public static Materialized<String, ElectionSummary, KeyValueStore<Bytes, byte[]>> materialize() {
    return Materialized.<String, ElectionSummary, KeyValueStore<Bytes, byte[]>>as("votes.integrity.aggregate")
            .withKeySerde(Serdes.String())
            .withValueSerde(StreamUtils.getJsonSerde(ElectionSummary.class));
  }
}
