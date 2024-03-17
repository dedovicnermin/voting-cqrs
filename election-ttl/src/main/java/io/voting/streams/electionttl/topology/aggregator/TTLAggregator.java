package io.voting.streams.electionttl.topology.aggregator;

import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.electionttl.model.Heartbeat;
import io.voting.streams.electionttl.model.TTLSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.WindowStore;

@Slf4j
public class TTLAggregator implements Aggregator<String, Heartbeat, TTLSummary> {

  public static Initializer<TTLSummary> initializer() {
    return TTLSummary::new;
  }

  @Override
  public TTLSummary apply(String electionIdKey, Heartbeat heartbeat, TTLSummary ttlSummary) {
    log.trace("Applying aggregate using key ({}), heartbeat ({}) and current TTL summary ({})", electionIdKey, heartbeat, ttlSummary);
    ttlSummary.add(electionIdKey, heartbeat);
    log.trace("TTL summary post-aggregate result : {}", ttlSummary);
    return ttlSummary;
  }


  public static Materialized<String, TTLSummary, WindowStore<Bytes, byte[]>> materialize() {
    return Materialized.<String, TTLSummary, WindowStore<Bytes, byte[]>>as("election.ttl.aggregate")
            .withKeySerde(Serdes.String())
            .withValueSerde(StreamUtils.getJsonSerde(TTLSummary.class));
  }
}
