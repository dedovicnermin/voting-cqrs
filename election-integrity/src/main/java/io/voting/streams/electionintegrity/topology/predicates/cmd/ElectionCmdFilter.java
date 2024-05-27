package io.voting.streams.electionintegrity.topology.predicates.cmd;

import io.voting.events.cmd.CreateElection;
import org.apache.kafka.streams.kstream.Named;

public class ElectionCmdFilter implements CmdTypeFilter {
  @Override
  public String ceTypeTarget() {
    return CreateElection.class.getName();
  }

  @Override
  public Named name() {
    return Named.as("ei.cmd.filter");
  }
}
