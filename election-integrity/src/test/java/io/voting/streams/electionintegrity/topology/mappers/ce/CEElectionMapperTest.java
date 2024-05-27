package io.voting.streams.electionintegrity.topology.mappers.ce;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.enums.ElectionStatus;
import io.voting.events.integrity.IntegrityEvent;
import io.voting.events.integrity.NewElection;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CEElectionMapperTest {

  private final ValueMapper<NewElection, CloudEvent> mapper = new CEElectionMapper();

  @Test
  void test() {
    final NewElection election = NewElection.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAuthor("author")
            .setTitle("title")
            .setDescription("description")
            .setCategory(ElectionCategory.Random)
            .setCandidates(Map.of("Foo", 0L, "Bar", 0L))
            .setStatus(ElectionStatus.OPEN)
            .setStartTs(Instant.now())
            .setEndTs(Instant.now())
            .build();
    final CloudEvent actual = mapper.apply(election);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getSource()).isEqualTo(URI.create("https://"+ ElectionIntegrityTopology.class.getSimpleName()));
    assertThat(actual.getType()).isEqualTo(NewElection.class.getName());
    assertThat(actual.getSubject()).isEqualTo(ElectionCategory.Random.toString());
    assertThat(AvroCloudEventData.<IntegrityEvent>dataOf(actual.getData()).getLegalEvent()).isEqualTo(election);
  }

}