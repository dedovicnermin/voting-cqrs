package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionStatus;
import io.voting.persistence.eventsink.dao.ElectionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class ElectionTTLListener implements EventListener<String, CloudEvent> {

  private final ElectionDao dao;

  public ElectionTTLListener(final EventReceiver<String, CloudEvent> eventReceiver, final ElectionDao dao) {
    eventReceiver.addListener(this);
    this.dao = dao;
  }

  @Override
  public void onEvent(ReceiveEvent<String, CloudEvent> event) {
    final Optional<CloudEvent> cloudEvent = Optional.of(event)
            .filter(e -> Objects.isNull(e.getPOrE().getError()))
            .map(e -> e.getPOrE().getPayload())
            .filter(payload -> CloudEventTypes.ELECTION_EXPIRATION_EVENT.equals(payload.getType()));
    if (cloudEvent.isPresent()) {
      log.debug("Processing {} : {}", CloudEventTypes.ELECTION_EXPIRATION_EVENT, event);
      try {
        final Election election = dao.updateElectionStatus(cloudEvent.get().getId(), ElectionStatus.CLOSED);
        log.debug("Result of Election TTL event : {}", election);
      } catch (Exception e) {
        log.error("Unexpected exception occurred while processing election TTL event : {}", e.getMessage(), e);
      }
    }
  }
}
