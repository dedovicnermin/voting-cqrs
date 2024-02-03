package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class ElectionVoteListener implements EventListener<String, CloudEvent> {

  public ElectionVoteListener(final EventReceiver<String, CloudEvent> eventReceiver) {
    eventReceiver.addListener(this);
  }

  @Override
  public void onEvent(ReceiveEvent<String, CloudEvent> event) {
    final Optional<CloudEvent> cloudEvent = Optional.of(event)
            .filter(e -> Objects.isNull(e.getPOrE().getError()))
            .map(e -> e.getPOrE().getPayload())
            .filter(payload -> CloudEventTypes.ELECTION_VOTE_EVENT.equals(payload.getType()));
    if (cloudEvent.isPresent()) {
      log.info("{} : {}", CloudEventTypes.ELECTION_VOTE_EVENT, event);
    }

  }
}
