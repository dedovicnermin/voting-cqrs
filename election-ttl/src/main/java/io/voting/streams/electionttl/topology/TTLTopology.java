package io.voting.streams.electionttl.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.streams.electionttl.ElectionTTLConfig;
import io.voting.streams.electionttl.model.TTLSummary;
import io.voting.streams.electionttl.model.Heartbeat;
import io.voting.streams.electionttl.topology.aggregator.TTLAggregator;
import io.voting.streams.electionttl.topology.mappers.CloudEventMapper;
import io.voting.streams.electionttl.topology.mappers.HeartbeatMapper;
import io.voting.streams.electionttl.topology.mappers.TombstoneMapper;
import io.voting.streams.electionttl.topology.predicates.CETypeFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

@Slf4j
public final class TTLTopology {

  private static final ValueMapper<TTLSummary, CloudEvent> cloudEventMapper = new CloudEventMapper();
  private static final Predicate<String, CloudEvent> targetSubsetEventTypes = new CETypeFilter();
  private static final ValueMapper<CloudEvent, Heartbeat> convertToHeartbeat = new HeartbeatMapper();
  private static final Aggregator<String, Heartbeat, TTLSummary> aggregator = new TTLAggregator();
  private static final KeyValueMapper<String, String, KeyValue<String, Void>> tombstoneMapper = new TombstoneMapper();
  private static final Consumed<String, CloudEvent> consumedWithStringCloudEvent = Consumed.with(Serdes.String(), StreamUtils.getCESerde());

  private TTLTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final KStream<String, TTLSummary> ttlStream = builder.stream(ingestTopics(properties), consumedWithStringCloudEvent)
            .filter(targetSubsetEventTypes)
            .mapValues(convertToHeartbeat)
            .groupByKey()
            .windowedBy(TimeWindows.ofSizeAndGrace(ttlDuration(properties), Duration.ofSeconds(1)))
            .aggregate(
                    TTLAggregator.initializer(),
                    aggregator,
                    TTLAggregator.materialize()
            )
            .suppress(Suppressed.untilWindowCloses(Suppressed.BufferConfig.unbounded().withMaxRecords(200000L)))
            .toStream()
            .map((windowedKey, ttlSummary) -> new KeyValue<>(windowedKey.key(), ttlSummary));

    ttlStream.flatMapValues(TTLSummary::getVoterList)
            .map(tombstoneMapper)
            .to(
                    voteChangelogTopic(properties),
                    Produced.with(Serdes.String(), Serdes.Void())
            );

    ttlStream.mapValues(cloudEventMapper)
            .to(ttlEventTopic(properties), Produced.with(Serdes.String(), StreamUtils.getCESerde()));

    return builder.build();
  }

  private static Collection<String> ingestTopics(final Properties properties) {
    return Arrays.asList(
            properties.getProperty(ElectionTTLConfig.ELECTION_REQUESTS_TOPIC),
            properties.getProperty(ElectionTTLConfig.ELECTION_VOTES_TOPIC)
    );
  }

  private static Duration ttlDuration(final Properties properties) {
    return Duration.parse(properties.getProperty(ElectionTTLConfig.ELECTION_TTL));
  }

  private static String voteChangelogTopic(final Properties properties) {
    return properties.getProperty(ElectionTTLConfig.VOTE_INTEGRITY_CHANGELOG_TOPIC);
  }

  private static String ttlEventTopic(final Properties properties) {
    return properties.getProperty(ElectionTTLConfig.ELECTION_REQUESTS_TOPIC);
  }

}
