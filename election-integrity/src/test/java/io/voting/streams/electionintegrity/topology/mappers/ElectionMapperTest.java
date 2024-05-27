package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.events.cmd.CreateElection;
import io.voting.events.enums.ElectionCategory;
import io.voting.events.enums.ElectionStatus;
import io.voting.events.integrity.NewElection;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElectionMapperTest {

  private static final String AUTHOR = "test";
  private static final String TITLE = "a_title";
  private static final String DESC = "a_description";
  private static final List<CharSequence> CANDIDATES = Arrays.asList("candidateX", "candidateY");

  private final ElectionMapper mapper = new ElectionMapper("PT15M");

  @Test
  void test() {
    final CreateElection electionCreate = CreateElection.newBuilder()
            .setAuthor(AUTHOR)
            .setTitle(TITLE)
            .setDescription(DESC)
            .setCategory(ElectionCategory.Random)
            .setCandidates(CANDIDATES)
            .build();

    final NewElection expected = NewElection.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAuthor(AUTHOR)
            .setTitle(TITLE)
            .setDescription(DESC)
            .setCategory(ElectionCategory.Random)
            .setCandidates(Map.of("candidateX", 0L, "candidateY", 0L))
            .setStartTs(Instant.now())
            .setEndTs(Instant.now())
            .setStatus(ElectionStatus.OPEN)
            .build();


    final NewElection actual = mapper.apply(electionCreate);
    final Instant actualBegin = actual.getStartTs();
    final Long expectedEnd = actualBegin.plus(Duration.ofMinutes(15)).toEpochMilli();


    assertAll(
            () -> assertThat(actual.getId()).isNotNull(),
            () -> assertThat(actual.getAuthor()).isEqualTo(expected.getAuthor()),
            () -> assertThat(actual.getTitle()).isEqualTo(expected.getTitle()),
            () -> assertThat(actual.getDescription()).isEqualTo(expected.getDescription()),
            () -> assertThat(actual.getCategory()).isEqualTo(expected.getCategory()),
            () -> assertThat(actual.getCandidates()).isEqualTo(expected.getCandidates()),
            () -> assertThat(actual.getEndTs().toEpochMilli()).isEqualTo(expectedEnd),
            () -> assertThat(actual.getStatus()).isEqualTo(ElectionStatus.OPEN)
    );


  }

}