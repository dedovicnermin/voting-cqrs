package io.voting.streams.electionttl.topology.predicates;

import io.cloudevents.CloudEvent;
import io.voting.streams.electionttl.topology.TestCEMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CETypeFilterTest {

  final Predicate<String, CloudEvent> predicate = new CETypeFilter();

  @Test
  void testValidCloudEventTypes() {
    assertThat(predicate.test(null, TestCEMapper.buildElectionCreate(UUID.randomUUID().toString())))
            .isTrue();
    assertThat(predicate.test(null, TestCEMapper.buildElectionVote(UUID.randomUUID().toString(), UUID.randomUUID().toString())))
            .isTrue();
  }

  @Test
  void testInvalidCloudEventTypes() {
    assertThat(predicate.test(null, TestCEMapper.buildExpectedTTLEvent(UUID.randomUUID().toString())))
            .isFalse();
  }

}