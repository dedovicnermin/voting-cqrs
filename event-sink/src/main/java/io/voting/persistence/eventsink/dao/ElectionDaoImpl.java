package io.voting.persistence.eventsink.dao;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionVote;
import io.voting.persistence.eventsink.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElectionDaoImpl implements ElectionDao {

  private final ElectionRepository repository;

  public Election insertElection(final Election election) {
    log.debug("Inserting new election ({})", election);
    return repository.insert(election);
  }

  public Election updateElection(final ElectionVote vote) {
    log.debug("Retrieving election state based on vote event : {}", vote);
    final Election election = repository.findById(vote.getElectionId())
            .orElseThrow(() -> new RuntimeException("Could not find election with ID : " + vote.getElectionId()));
    log.debug("Election retrieved from DB {}", election);

    final Map<String, Long> candidateScoreMap = Optional.of(election)
            .map(Election::getCandidates)
            .orElseThrow(() -> new RuntimeException("Could not retrieve candidates from election : " + election));
    final Long updatedCandidateScore = candidateScoreMap.get(vote.getVotedFor()) + 1;
    candidateScoreMap.put(vote.getVotedFor(), updatedCandidateScore);

    log.debug("Election with ID ({}) will contain updated election candidate scores : {}", vote.getElectionId(), candidateScoreMap);
    return repository.save(election);
  }

}
