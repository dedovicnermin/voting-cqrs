package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.events.cmd.CreateElection;
import io.voting.events.enums.ElectionStatus;
import io.voting.events.integrity.NewElection;
import io.voting.streams.electionintegrity.topology.util.TTLPairs;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.javatuples.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class ElectionMapper implements ValueMapper<CreateElection, NewElection> {

  private final Duration ttlDuration;

  /**
   * Map election list into candidate map
   * Map start timestamp and calculate end timestamp based on ttl
   * @param ttlDuration Duration.parse() for formatting
   */
  public ElectionMapper(final String ttlDuration) {
    this.ttlDuration = Duration.parse(ttlDuration);
  }

  @Override
  public NewElection apply(CreateElection electionCreate) {
    return Optional.of(electionCreate)
            .map(CreateElection::getCandidates)
            .map(cList -> {
              final Map<CharSequence, Long> scoreInit = new HashMap<>();
              for (final CharSequence candidate : cList) {
                scoreInit.put(candidate, 0L);
              }
              log.debug("Initialized candidate scores : {}", cList);
              return scoreInit;
            })
            .map(scoreInit -> {
              final Pair<Instant, Instant> ttlPair = TTLPairs.now(ttlDuration);
              final NewElection election = NewElection.newBuilder()
                      .setId(UUID.randomUUID().toString())
                      .setAuthor(electionCreate.getAuthor())
                      .setTitle(electionCreate.getTitle())
                      .setDescription(electionCreate.getDescription())
                      .setCategory(electionCreate.getCategory())
                      .setCandidates(scoreInit)
                      .setStatus(ElectionStatus.OPEN)
                      .setStartTs(ttlPair.getValue0())
                      .setEndTs(ttlPair.getValue1())
                      .build();
              log.trace("Enrichment result : {}", election);
              return election;
            })
            .orElseThrow();
  }

  public static Named name() {
    return Named.as("ei.legal.election.mapper");
  }
}
