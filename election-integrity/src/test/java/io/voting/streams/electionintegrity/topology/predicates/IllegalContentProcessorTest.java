package io.voting.streams.electionintegrity.topology.predicates;

import io.voting.events.cmd.CreateElection;
import io.voting.events.enums.ElectionCategory;
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
  private static final List<CharSequence> CANDIDATES = Collections.singletonList("good_candidate");

  private static IllegalContentProcessor processor;

  @BeforeAll
  static void init() {
    processor = new IllegalContentProcessor();
  }

  @Test
  void safeEvent() {
    final CreateElection electionCreate = CreateElection.newBuilder()
            .setAuthor(AUTHOR)
            .setTitle(TITLE)
            .setDescription(DESC)
            .setCategory(ElectionCategory.Random)
            .setCandidates(CANDIDATES)
            .build();
    assertThat(processor.test(null, electionCreate)).isFalse();
  }

  @Test
  void illegalTitle() {
    final CreateElection electionCreate = CreateElection.newBuilder()
            .setAuthor(AUTHOR)
            .setTitle("asshole")
            .setDescription(DESC)
            .setCategory(ElectionCategory.Random)
            .setCandidates(CANDIDATES)
            .build();
    assertThat(processor.test(null, electionCreate)).isTrue();
  }

  @Test
  void illegalDescription() {
    final CreateElection electionCreate = CreateElection.newBuilder()
            .setAuthor(AUTHOR)
            .setTitle(TITLE)
            .setDescription("assh0le")
            .setCategory(ElectionCategory.Random)
            .setCandidates(CANDIDATES)
            .build();
    assertThat(processor.test(null, electionCreate)).isTrue();
  }


  @Test
  void illegalCandidate() {
    final CreateElection electionCreate = CreateElection.newBuilder()
            .setAuthor(AUTHOR)
            .setTitle(TITLE)
            .setDescription(DESC)
            .setCategory(ElectionCategory.Random)
            .setCandidates(Arrays.asList("Bob", "Asshton", "Bitch"))
            .build();
    assertThat(processor.test(null, electionCreate)).isTrue();

    final CreateElection actuallySafe = CreateElection.newBuilder()
            .setAuthor(AUTHOR)
            .setTitle(TITLE)
            .setDescription(DESC)
            .setCategory(ElectionCategory.Random)
            .setCandidates(Arrays.asList("Bob", "Asshton"))
            .build();
    assertThat(processor.test(null, actuallySafe)).isFalse();
  }

}