package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.RegisterVote;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
public class VoteCmdMapper implements CloudEventMapper<String, ElectionVote> {

  @Override
  public CloudEvent apply(String key, ElectionVote electionVote) {
    log.trace("Applying CE transformation (K,V): {}, {}", key, electionVote);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(RegisterVote.class.getName())
            .withSubject(electionVote.getElectionId())
            .withData(AvroCloudEventData.MIME_TYPE, avroData(key, electionVote))
            .withTime(OffsetDateTime.now())
            .build();
    log.trace("Applied CE transformation: {}", event);
    return event;
  }

  @Override
  public CmdEvent format(String key, ElectionVote value) {
    return new CmdEvent(
            RegisterVote.newBuilder()
                    .setEId(value.getElectionId())
                    .setVotedFor(value.getVotedFor())
                    .build()
    );
  }

}
