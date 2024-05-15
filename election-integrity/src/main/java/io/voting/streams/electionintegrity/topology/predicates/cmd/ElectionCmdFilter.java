package io.voting.streams.electionintegrity.topology.predicates.cmd;

import io.voting.common.library.kafka.utils.CloudEventTypes;
import org.apache.kafka.streams.kstream.Named;

public class ElectionCmdFilter implements CmdTypeFilter {
  @Override
  public String ceTypeTarget() {
    return CloudEventTypes.ELECTION_CREATE_CMD;
  }

  @Override
  public Named name() {
    return Named.as("ei.cmd.filter");
  }
}
