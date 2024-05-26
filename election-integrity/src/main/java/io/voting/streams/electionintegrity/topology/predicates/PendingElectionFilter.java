package io.voting.streams.electionintegrity.topology.predicates;

import io.voting.events.cmd.ViewElection;
import io.voting.events.enums.ElectionView;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;

public class PendingElectionFilter implements Predicate<String, ViewElection> {

  @Override
  public boolean test(final String k, final ViewElection v) {
    return ElectionView.PENDING == v.getView();
  }

  public static Named name() {
    return Named.as("ttl.pending.view.filter");
  }
}
