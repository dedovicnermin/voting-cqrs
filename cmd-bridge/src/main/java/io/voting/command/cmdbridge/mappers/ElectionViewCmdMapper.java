package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionView;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.ViewElection;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
public class ElectionViewCmdMapper implements CloudEventMapper<String, ElectionView> {

  @Override
  public CloudEvent apply(String key, ElectionView electionView) {
    log.trace("Applying transformation (K,V): {}, {}", key, electionView);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(ViewElection.class.getName())
            .withSubject(key)
            .withData(AvroCloudEventData.MIME_TYPE, avroData(key, electionView))
            .withTime(OffsetDateTime.now())
            .build();
    log.trace("Applied transformation: {}", event);
    return event;
  }

  @Override
  public CmdEvent format(String electionId, ElectionView view) {
    return new CmdEvent(
            new ViewElection(electionId, io.voting.events.enums.ElectionView.valueOf(view.name()))
    );
  }
}
