package io.voting.persistence.eventsink.listener;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.listener.EventListener;
import io.voting.common.library.kafka.clients.receiver.EventReceiver;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionStatus;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
import io.voting.persistence.eventsink.dao.ElectionDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
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
            .filter(payload -> NewElection.class.getName().equals(payload.getType()));
    if (cloudEvent.isPresent()) {
      log.debug("Processing {} : {}", NewElection.class.getName(), event);
      try {
        final NewElection newElectionData = (NewElection) AvroCloudEventData.<IntegrityEvent>dataOf(cloudEvent.get().getData()).getLegalEvent();
        final Election election = dao.insertElection(mapNewElection(newElectionData));
        log.debug("Result of election insert : {}", election);
      } catch (Exception e) {
        log.error("Unexpected exception occurred during election insert : {}", e.getMessage(), e);
      }
    }
  }

  private Election mapNewElection(final NewElection newElection) {
    final Map<String, Long> cMap = new HashMap<>();
    newElection.getCandidates().forEach((k,v) -> cMap.put(k.toString(), v));
    return Election.builder()
            .id(newElection.getId().toString())
            .author(newElection.getAuthor().toString())
            .title(newElection.getTitle().toString())
            .description(newElection.getDescription().toString())
            .category(newElection.getCategory().name())
            .status(ElectionStatus.valueOf(newElection.getStatus().name()))
            .candidates(cMap)
            .startTs(newElection.getStartTs().toEpochMilli())
            .endTs(newElection.getEndTs().toEpochMilli())
            .build();
  }
}
