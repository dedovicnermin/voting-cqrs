package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.models.ElectionCreate;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.CreateElection;
import io.voting.events.enums.ElectionCategory;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Slf4j
public class ElectionCreateCmdMapper implements CloudEventMapper<String, ElectionCreate> {

  @Override
  public CloudEvent apply(String key, ElectionCreate electionCreate) {
    log.trace("Applying CE transformation (K,V): {}, {}", key, electionCreate);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(CreateElection.class.getName())
            .withSubject(electionCreate.getCategory())
            .withData(AvroCloudEventData.MIME_TYPE, avroData(key, electionCreate))
            .withTime(OffsetDateTime.now())
            .build();
    log.trace("Applied CE transformation: {}", event);
    return event;
  }

  @Override
  public CmdEvent format(String key, ElectionCreate electionCreate) {
    try {
      ElectionCategory category = ElectionCategory.valueOf(electionCreate.getCategory());
      return new CmdEvent(buildCreateElection(electionCreate, category));
    } catch (IllegalArgumentException e) {
      log.error("Unsupported ElectionCategory ({}) present in payload : {}", electionCreate.getCategory(), electionCreate);
      return new CmdEvent(buildCreateElection(electionCreate, ElectionCategory.Unknown));
    }
  }

  private CreateElection buildCreateElection(final ElectionCreate electionCreate, final ElectionCategory category) {
    return CreateElection.newBuilder()
            .setAuthor(electionCreate.getAuthor())
            .setTitle(electionCreate.getTitle())
            .setDescription(electionCreate.getDescription())
            .setCategory(category)
            .setCandidates(new ArrayList<>(electionCreate.getCandidates()))
            .build();
  }

}
