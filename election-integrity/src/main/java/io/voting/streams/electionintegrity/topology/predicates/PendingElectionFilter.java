package io.voting.streams.electionintegrity.topology.predicates;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionView;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;

public class PendingElectionFilter implements Predicate<String, CloudEvent> {

  @Override
  public boolean test(final String k, final CloudEvent v) {
    return ElectionView.PENDING == StreamUtils.unwrapCloudEventData(v.getData(), ElectionView.class);
  }

  public static Named name() {
    return Named.as("ttl.pending.view.filter");
  }
}
