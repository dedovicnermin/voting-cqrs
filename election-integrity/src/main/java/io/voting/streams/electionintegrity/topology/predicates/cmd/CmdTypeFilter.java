package io.voting.streams.electionintegrity.topology.predicates.cmd;

import io.cloudevents.CloudEvent;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;

public interface CmdTypeFilter extends Predicate<String, CloudEvent> {

  String ceTypeTarget();
  Named name();

  @Override
  default boolean test(String s, CloudEvent cloudEvent) {
    return ceTypeTarget().equals(cloudEvent.getType());
  }
}
