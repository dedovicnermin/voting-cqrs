package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.persistence.eventsink.dao.ElectionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Interested in election create events
 */
@Slf4j
@Component
public class ElectionCreateListener implements EventListener<String, CloudEvent> {

  private final ElectionDao dao;

  public ElectionCreateListener(final EventReceiver<String, CloudEvent> receiver, final ElectionDao dao) {
    receiver.addListener(this);
    this.dao = dao;
  }

  @Override
  public void onEvent(ReceiveEvent<String, CloudEvent> event) {
    final Optional<CloudEvent> cloudEvent = Optional.of(event)
            .filter(e -> Objects.isNull(e.getPOrE().getError()))
            .map(e -> e.getPOrE().getPayload())
            .filter(payload -> CloudEventTypes.ELECTION_CREATE_EVENT.equals(payload.getType()));
    if (cloudEvent.isPresent()) {
      log.debug("Processing {} : {}", CloudEventTypes.ELECTION_CREATE_EVENT, event);
      try {
        final Election election = dao.insertElection(StreamUtils.unwrapCloudEventData(cloudEvent.get().getData(), Election.class));
        log.debug("Result of election insert : {}", election);
      } catch (Exception e) {
        log.error("Unexpected exception occurred during election insert : {}", e.getMessage(), e);
      }

    }
  }
}
