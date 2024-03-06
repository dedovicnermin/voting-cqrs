package io.voting.streams.electionttl.topology.mappers;

import io.cloudevents.CloudEvent;
import io.voting.streams.electionttl.model.Heartbeat;
import org.apache.kafka.streams.kstream.ValueMapper;

/**
 * CloudEventTypes.ELECTION_CREATE_EVENT && CloudEventTypes.ELECTION_VOTE_EVENT
 * both map respective IDs in headers/metadata.
 */
public class HeartbeatMapper implements ValueMapper<CloudEvent, Heartbeat> {
  @Override
  public Heartbeat apply(CloudEvent cloudEvent) {
    return new Heartbeat(cloudEvent.getId(), cloudEvent.getType());
  }
}
