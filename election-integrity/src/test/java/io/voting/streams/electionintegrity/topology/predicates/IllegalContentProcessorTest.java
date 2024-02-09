package io.voting.streams.electionintegrity.topology.predicates;

import io.voting.common.library.models.ElectionCreate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IllegalContentProcessorTest {

  private static final String AUTHOR = "test";
  private static final String TITLE = "good_title";
  private static final String DESC = "good_description";
  private static final String CATEGORY = "good_category";
  private static final List<String> CANDIDATES = Collections.singletonList("good_candidate");

  private static IllegalContentProcessor processor;

  @BeforeAll
  static void init() {
    processor = new IllegalContentProcessor();
  }

  @Test
  void safeEvent() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, TITLE, DESC, CATEGORY, CANDIDATES);
    assertThat(processor.test(null, electionCreate)).isFalse();
  }

  @Test
  void illegalTitle() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, "asshole", DESC, CATEGORY, CANDIDATES);
    assertThat(processor.test(null, electionCreate)).isTrue();
  }

  @Test
  void illegalDescription() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, TITLE, "assh0le", CATEGORY, CANDIDATES);
    assertThat(processor.test(null, electionCreate)).isTrue();
  }

  @Test
  void illegalCategory() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, TITLE, DESC, "FUCK", CANDIDATES);
    assertThat(processor.test(null, electionCreate)).isTrue();
  }

  @Test
  void illegalCandidate() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, TITLE, DESC, CATEGORY, Arrays.asList("Bob", "Asshton", "Bitch"));
    assertThat(processor.test(null, electionCreate)).isTrue();

    final ElectionCreate actuallySafe = new ElectionCreate(AUTHOR, TITLE, DESC, CATEGORY, Arrays.asList("Bob", "Asshton"));
    assertThat(processor.test(null, actuallySafe)).isFalse();
  }

}