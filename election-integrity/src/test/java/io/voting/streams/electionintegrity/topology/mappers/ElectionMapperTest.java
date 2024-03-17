package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ElectionMapperTest {

  private static final String AUTHOR = "test";
  private static final String TITLE = "a_title";
  private static final String DESC = "a_description";
  private static final String CATEGORY = "a_category";
  private static final List<String> CANDIDATES = Arrays.asList("candidateX", "candidateY");

  private final ElectionMapper mapper = new ElectionMapper("PT15M");

  @Test
  void test() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, TITLE, DESC, CATEGORY, CANDIDATES);
    final Election expected = Election.builder()
            .author(AUTHOR)
            .title(TITLE)
            .description(DESC)
            .category(CATEGORY)
            .candidates(Map.of("candidateX", 0L, "candidateY", 0L))
            .build();


    final Election actual = mapper.apply(electionCreate);
    final Long actualBegin = actual.getStartTs();
    final Long expectedEnd = Instant.ofEpochMilli(actualBegin).plus(Duration.ofMinutes(15)).toEpochMilli();


    assertAll(
            () -> assertThat(actual.getId()).isNotNull(),
            () -> assertThat(actual.getAuthor()).isEqualTo(expected.getAuthor()),
            () -> assertThat(actual.getTitle()).isEqualTo(expected.getTitle()),
            () -> assertThat(actual.getDescription()).isEqualTo(expected.getDescription()),
            () -> assertThat(actual.getCategory()).isEqualTo(expected.getCategory()),
            () -> assertThat(actual.getCandidates()).isEqualTo(expected.getCandidates()),
            () -> assertThat(actual.getEndTs()).isEqualTo(expectedEnd),
            () -> assertThat(actual.getStatus()).isEqualTo(ElectionStatus.OPEN)
    );


  }

}