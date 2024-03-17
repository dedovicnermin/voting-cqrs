package io.voting.streams.voteintegrity.topology.mappers;

import io.voting.common.library.models.ElectionVote;
import io.voting.streams.voteintegrity.model.ElectionSummary;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElectionIdExtractorTest {

  static final String USER_ID = "111";
  static final String ELECTION_ID = "777";
  static final String KEY = USER_ID + ":" + ELECTION_ID;

  final KeyValueMapper<String, ElectionSummary, String> extractor = new ElectionIdExtractor();
  ElectionSummary summary;

  @BeforeEach
  void setup() {
    summary = new ElectionSummary();
  }

  @Test
  void testExtractionOnValidKey() {
    summary.add(KEY, ElectionVote.of("thisWillNotBeReturned_KeyIsValidFormat", "Foo"));
    assertThat(extractor.apply(KEY, summary)).isEqualTo(ELECTION_ID);
  }

  @Test
  void testExtractionOnInvalidKey() {
    summary.add(KEY, ElectionVote.of(ELECTION_ID, "Foo"));
    assertThat(extractor.apply(USER_ID, summary)).isEqualTo(ELECTION_ID);
  }

  @Test
  void testExtractionOnEmptyKey() {
    summary.add(KEY, ElectionVote.of(ELECTION_ID, "Foo"));
    assertThat(extractor.apply("", summary)).isEqualTo(ELECTION_ID);
  }

  @Test
  void testExtractionOnNullKey() {
    summary.add(KEY, ElectionVote.of(ELECTION_ID, "Foo"));
    assertThat(extractor.apply(null, summary)).isEqualTo(ELECTION_ID);
  }
}
