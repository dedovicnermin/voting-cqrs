package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.models.ElectionView;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.ViewElection;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
public class ElectionViewCmdMapper implements CloudEventMapper<String, ElectionView> {

  @Override
  public CloudEvent apply(String key, ElectionView electionView) {
    log.trace("Applying CE transformation (K,V): {}, {}", key, electionView);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(ViewElection.class.getName())
            .withSubject(key)
            .withData(AvroCloudEventData.MIME_TYPE, avroData(key, electionView))
            .withTime(OffsetDateTime.now())
            .build();
    log.trace("Applied CE transformation: {}", event);
    return event;
  }

  @Override
  public CmdEvent format(String electionId, ElectionView view) {
    try {
      io.voting.events.enums.ElectionView eView = io.voting.events.enums.ElectionView.valueOf(view.name());
      return new CmdEvent(buildViewElection(electionId, eView));
    } catch (IllegalArgumentException e) {
      log.error("Unsupported election view ({}) present in election view payload", view.name());
      return new CmdEvent(buildViewElection(electionId, io.voting.events.enums.ElectionView.UNKNOWN));
    }
  }

  private ViewElection buildViewElection(final String electionId, final io.voting.events.enums.ElectionView view) {
    return ViewElection.newBuilder()
            .setEId(electionId)
            .setView(view)
            .build();
  }
}
