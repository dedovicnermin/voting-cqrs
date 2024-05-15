package io.voting.streams.electionintegrity.model;

import io.voting.common.library.kafka.utils.CloudEventTypes;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class TTLSummary {

  private String electionId;
  private List<String> voterList = new ArrayList<>();

  public TTLSummary add(final String electionIdKey, final ElectionHeartbeat event) {
    // election cannot be viewed or voted for without the creation of the election
    if (Objects.isNull(electionId)) {
      electionId = electionIdKey;
    }

    if (CloudEventTypes.ELECTION_VOTE_EVENT.equals(event.type())) {
      voterList.add(event.id());
      return this;
    }
    return this;
  }
}
