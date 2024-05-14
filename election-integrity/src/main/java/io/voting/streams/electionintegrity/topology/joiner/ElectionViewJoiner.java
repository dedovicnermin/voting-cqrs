package io.voting.streams.electionintegrity.topology.joiner;

import io.cloudevents.CloudEvent;
import io.voting.common.library.models.ElectionView;
import io.voting.streams.electionintegrity.model.ElectionHeartbeat;
import org.apache.kafka.streams.kstream.ValueJoiner;

import java.util.Optional;

/**
 * Join legal elections/votes with ElectionView to make ElectionHeartbeat
 * Legal votes are keyed by electionId
 * Legal elections are keyed by electionId
 * Election views are keyed by electionId
 */
public class ElectionViewJoiner implements ValueJoiner<CloudEvent, ElectionView, ElectionHeartbeat> {
  @Override
  public ElectionHeartbeat apply(CloudEvent cloudEvent, ElectionView view) {
    final ElectionView electionView = Optional.ofNullable(view).orElse(ElectionView.OPEN);
    return new ElectionHeartbeat(cloudEvent.getId(), cloudEvent.getType(), electionView);
  }
}
