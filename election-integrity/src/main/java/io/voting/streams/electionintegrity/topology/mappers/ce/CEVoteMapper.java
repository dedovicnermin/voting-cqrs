package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewVote;
import io.voting.streams.electionintegrity.model.ElectionSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;

import java.time.OffsetDateTime;

@Slf4j
public class CEVoteMapper implements CEMapper<ElectionSummary> {
  @Override
  public CloudEvent apply(ElectionSummary electionSummary) {
    log.trace("Transforming legal vote into CloudEvent format : {}", electionSummary);
    final ElectionVote vote = electionSummary.getVote();

    final CloudEvent event = ceBuilder
            .withId(electionSummary.getUser())
            .withType(NewVote.class.getName())
            .withSubject(vote.getElectionId())
            .withData(AvroCloudEventData.MIME_TYPE, avroData(electionSummary))
            .withTime(OffsetDateTime.now())
            .build();

    log.debug("Legal vote transformation result : {}", event);
    return event;
  }

  public static Named name() {
    return Named.as("vi.ce.mapper");
  }

  @Override
  public IntegrityEvent format(ElectionSummary value) {
    return new IntegrityEvent(
            NewVote.newBuilder()
                    .setEId(value.getVote().getElectionId())
                    .setCandidate(value.getVote().getVotedFor())
                    .setUId(value.getUser())
                    .build()
    );
  }
}
