package io.voting.persistence.eventsink.dao;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionVote;

public interface ElectionDao {

  Election insertElection(Election election);
  Election updateElection(ElectionVote electionVote);

}
