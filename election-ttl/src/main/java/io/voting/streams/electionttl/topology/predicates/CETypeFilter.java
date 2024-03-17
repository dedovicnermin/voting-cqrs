package io.voting.streams.electionttl.topology.predicates;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import org.apache.kafka.streams.kstream.Predicate;

public class CETypeFilter implements Predicate<String, CloudEvent> {

  @Override
  public boolean test(String s, CloudEvent cloudEvent) {
    return CloudEventTypes.ELECTION_CREATE_EVENT.equals(cloudEvent.getType())
            || CloudEventTypes.ELECTION_VOTE_EVENT.equals(cloudEvent.getType());
  }
}
