package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.models.ReceiveEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class ErrorEventListener implements EventListener<String, CloudEvent> {

  public ErrorEventListener(final EventReceiver<String, CloudEvent> eventReceiver) {
    eventReceiver.addListener(this);
  }

  @Override
  public void onEvent(ReceiveEvent<String, CloudEvent> event) {
    Optional.of(event)
            .map(ReceiveEvent::getPOrE)
            .filter(pOrE -> Objects.nonNull(pOrE.getError()))
            .ifPresent(pOrE -> log.error("ReceiveEvent containing error found : {}", event));
  }
}
