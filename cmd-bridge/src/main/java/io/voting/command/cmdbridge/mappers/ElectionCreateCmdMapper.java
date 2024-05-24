package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.models.ElectionCreate;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.CreateElection;
import io.voting.events.enum$.ElectionCategory;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
public class ElectionCreateCmdMapper implements CloudEventMapper<String, ElectionCreate> {

  @Override
  public CloudEvent apply(String key, ElectionCreate electionCreate) {
    log.trace("Applying transformation (K,V): {}, {}", key, electionCreate);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(CreateElection.class.getName())
            .withSubject(electionCreate.getCategory())
            .withData(AvroCloudEventData.MIME_TYPE, avroData(key, electionCreate))
            .withTime(OffsetDateTime.now())
            .build();
    log.trace("Applied transformation: {}", event);
    return event;
  }

  @Override
  public CmdEvent format(String key, ElectionCreate electionCreate) {
    return new CmdEvent(
            new CreateElection(
                    electionCreate.getAuthor(),
                    electionCreate.getTitle(),
                    electionCreate.getDescription(),
                    Optional.of(ElectionCategory.valueOf(electionCreate.getCategory())).orElse(ElectionCategory.Unknown),
                    new ArrayList<>(electionCreate.getCandidates())
            )
    );
  }

}
