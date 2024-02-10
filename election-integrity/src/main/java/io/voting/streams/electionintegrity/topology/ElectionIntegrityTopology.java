package io.voting.streams.electionintegrity.topology;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.streams.electionintegrity.topology.mappers.CandidateMapper;
import io.voting.streams.electionintegrity.topology.mappers.CloudEventMapper;
import io.voting.streams.electionintegrity.topology.predicates.IllegalContentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.Properties;

@Slf4j
public final class ElectionIntegrityTopology {

  private static final Predicate<String, ElectionCreate> illegalContent = new IllegalContentProcessor();
  private static final ValueMapper<ElectionCreate, Election> candidateEnrichment = new CandidateMapper();
  private static final ValueMapper<Election, CloudEvent> cloudEventEnrichment = new CloudEventMapper();

  private ElectionIntegrityTopology() {}

  public static Topology buildTopology(final StreamsBuilder builder, final Properties properties) {
    final String inputTopic = properties.getProperty("input.topic");
    final String outputTopic = properties.getProperty("output.topic");
    log.debug("Input topic : {}", inputTopic);
    log.debug("Output topic : {}", outputTopic);

    builder.stream(inputTopic, Consumed.with(Serdes.String(), StreamUtils.getJsonSerde(ElectionCreate.class)))
            .filterNot(illegalContent)
            .mapValues(candidateEnrichment)
            .mapValues(cloudEventEnrichment)
            .to(outputTopic, Produced.with(Serdes.String(), StreamUtils.getCESerde()));

    final Topology topology = builder.build();
    log.debug("=== Topology === \n{}", topology.describe());
    return topology;
  }

}
