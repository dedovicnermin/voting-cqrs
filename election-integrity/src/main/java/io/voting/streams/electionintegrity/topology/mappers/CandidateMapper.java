package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class CandidateMapper implements ValueMapper<ElectionCreate, Election> {

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
              final Election election = new Election(null, electionCreate.getAuthor(), electionCreate.getTitle(), electionCreate.getDescription(), electionCreate.getCategory(), scoreInit);
              log.debug("Enrichment result : {}", election);
              return election;
            })
            .orElseThrow();
  }
}
