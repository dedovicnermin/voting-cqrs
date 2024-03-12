package io.voting.streams.electionintegrity.topology.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionStatus;
import io.voting.streams.electionintegrity.topology.ElectionIntegrityTopology;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CloudEventMapperTest {

  private final ValueMapper<Election, CloudEvent> mapper = new CloudEventMapper();

  @Test
  void test() {
    final String category = "TEST";
    final Election election = Election.builder()
            .id(UUID.randomUUID().toString())
            .author("author")
            .title("title")
            .description("description")
            .category(category)
            .candidates(Map.of("Foo", 0L, "Bar", 0L))
            .status(ElectionStatus.OPEN)
            .startTs(Instant.now().toEpochMilli())
            .build();
    final CloudEvent actual = mapper.apply(election);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getSource()).isEqualTo(URI.create("https://"+ ElectionIntegrityTopology.class.getSimpleName()));
    assertThat(actual.getType()).isEqualTo(CloudEventTypes.ELECTION_CREATE_EVENT);
    assertThat(actual.getSubject()).isEqualTo(category);
    assertThat(StreamUtils.unwrapCloudEventData(actual.getData(), Election.class)).isEqualTo(election);
  }

}