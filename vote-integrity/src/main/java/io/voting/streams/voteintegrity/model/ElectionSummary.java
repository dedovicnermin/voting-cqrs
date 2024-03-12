package io.voting.streams.voteintegrity.model;

import io.voting.common.library.models.ElectionVote;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
public class ElectionSummary {

  private String user;
  private ElectionVote vote;
  private Long voteAttempts = 0L;

  public ElectionSummary add(final String key, final ElectionVote electionVote) {
    if (Objects.isNull(vote)) {
      user = key.split(":")[0];
      vote = electionVote;
    }
    voteAttempts++;
    return this;
  }

}
