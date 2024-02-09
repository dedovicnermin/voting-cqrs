package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionCreate;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateMapperTest {

  private static final String AUTHOR = "test";
  private static final String TITLE = "a_title";
  private static final String DESC = "a_description";
  private static final String CATEGORY = "a_category";
  private static final List<String> CANDIDATES = Arrays.asList("candidateX", "candidateY");

  private final CandidateMapper mapper = new CandidateMapper();

  @Test
  void test() {
    final ElectionCreate electionCreate = new ElectionCreate(AUTHOR, TITLE, DESC, CATEGORY, CANDIDATES);
    final Election expected = new Election(null, AUTHOR, TITLE, DESC, CATEGORY, Map.of("candidateX", 0L, "candidateY", 0L));

    assertThat(mapper.apply(electionCreate)).isEqualTo(expected);
  }

}