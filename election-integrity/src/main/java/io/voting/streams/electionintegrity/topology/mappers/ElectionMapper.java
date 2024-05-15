package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionStatus;
import io.voting.streams.electionintegrity.topology.util.TTLPairs;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.javatuples.Pair;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class ElectionMapper implements ValueMapper<ElectionCreate, Election> {

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
  public Election apply(ElectionCreate electionCreate) {
    return Optional.of(electionCreate)
            .map(ElectionCreate::getCandidates)
            .map(cList -> {
              final Map<String, Long> scoreInit = new HashMap<>();
              for (final String candidate : cList) {
                scoreInit.put(candidate, 0L);
              }
              log.debug("Initialized candidate scores : {}", cList);
              return scoreInit;
            })
            .map(scoreInit -> {
              final Pair<Long, Long> ttlPair = TTLPairs.now(ttlDuration);
              final Election election = Election.builder()
                      .id(UUID.randomUUID().toString())
                      .author(electionCreate.getAuthor())
                      .title(electionCreate.getTitle())
                      .description(electionCreate.getDescription())
                      .category(electionCreate.getCategory())
                      .candidates(scoreInit)
                      .status(ElectionStatus.OPEN)
                      .startTs(ttlPair.getValue0())
                      .endTs(ttlPair.getValue1())
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
