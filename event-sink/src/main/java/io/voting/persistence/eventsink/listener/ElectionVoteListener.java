package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewVote;
import io.voting.persistence.eventsink.dao.ElectionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class ElectionVoteListener implements EventListener<String, CloudEvent> {

  private final ElectionDao dao;

  public ElectionVoteListener(final EventReceiver<String, CloudEvent> eventReceiver, final ElectionDao dao) {
    eventReceiver.addListener(this);
    this.dao = dao;
  }

  @Override
  public void onEvent(ReceiveEvent<String, CloudEvent> event) {
    final Optional<CloudEvent> cloudEvent = Optional.of(event)
            .filter(e -> Objects.isNull(e.getPOrE().getError()))
            .map(e -> e.getPOrE().getPayload())
            .filter(payload -> NewVote.class.getName().equals(payload.getType()));
    if (cloudEvent.isPresent()) {
      log.debug("Processing {} : {}", NewVote.class.getName(), event);
      try {
        final NewVote newVoteData = (NewVote) AvroCloudEventData.<IntegrityEvent>dataOf(cloudEvent.get().getData()).getLegalEvent();
        final Election election = dao.updateElection(
                ElectionVote.of(newVoteData.getEId().toString(), newVoteData.getCandidate().toString())
        );
        log.debug("Result of election update : {}", election);
      } catch (Exception e) {
        log.error("Unexpected exception occurred during election update : {}", e.getMessage(), e);
      }
    }
  }
}
