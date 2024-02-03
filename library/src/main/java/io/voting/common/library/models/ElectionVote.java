package io.voting.common.library.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElectionVote {

  private String electionId;
  private String votedFor;

  @JsonIgnore
  public static ElectionVote of(final String electionId, final String votedFor) {
    return new ElectionVote(electionId, votedFor);
  }
}
