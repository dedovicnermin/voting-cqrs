package io.voting.streams.electionintegrity.topology.predicates.cmd;

import io.voting.events.cmd.RegisterVote;
import org.apache.kafka.streams.kstream.Named;

public class VoteCmdFilter implements CmdTypeFilter {
  @Override
  public String ceTypeTarget() {
    return RegisterVote.class.getName();
  }

  @Override
  public Named name() {
    return Named.as("vi.cmd.filter");
  }
}
