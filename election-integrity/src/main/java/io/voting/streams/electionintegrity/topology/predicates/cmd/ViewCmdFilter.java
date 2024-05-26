package io.voting.streams.electionintegrity.topology.predicates.cmd;

import io.voting.events.cmd.ViewElection;
import org.apache.kafka.streams.kstream.Named;

public class ViewCmdFilter implements CmdTypeFilter {
  @Override
  public String ceTypeTarget() {
    return ViewElection.class.getName();
  }

  @Override
  public Named name() {
    return Named.as("ttl.cmd.filter");
  }
}
