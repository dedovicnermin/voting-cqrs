package io.voting.streams.electionttl.topology.mappers;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TombstoneMapperTest {

  private KeyValueMapper<String, String, KeyValue<String, Void>> mapper;

  @BeforeEach
  void setup() {
    mapper = new TombstoneMapper();
  }

  @ParameterizedTest
  @ValueSource(strings = {"000", "001", "002", "003", "004", "005", "106", "107"})
  void test(String userId) {
    final String eId = "999";

    final KeyValue<String, Void> actual = mapper.apply(eId, userId);

    assertThat(actual.key).isEqualTo(userId + ":" + eId);
    assertThat(actual.value).isNull();
  }

}