package io.voting.streams.voteintegrity.topology.predicates;

import io.voting.streams.voteintegrity.model.ElectionSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Predicate;

@Slf4j
public class FirstVoteProcessor implements Predicate<String, ElectionSummary> {

  @Override
  public boolean test(final String key, final ElectionSummary electionSummary) {
    if (electionSummary.getVoteAttempts() > 1L) {
      log.info("Illegal vote encountered for key ({}) : total vote attempts - {}", key, electionSummary.getVoteAttempts());
      return false;
    } else if (electionSummary.getVoteAttempts() <= 0) {
      log.warn("Illegal unexpected vote encountered for key ({}) : {}", key, electionSummary);
      return false;
    } else {
      log.debug("Legal vote encountered for key ({}) : {}", key, electionSummary);
      return true;
    }
  }
}
