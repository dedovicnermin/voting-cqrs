package io.voting.streams.electionintegrity.topology.predicates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.voting.common.library.models.ElectionCreate;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Predicate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
public class IllegalContentProcessor implements Predicate<String, ElectionCreate> {

  private final ObjectMapper mapper;
  private Pattern pattern;

  public IllegalContentProcessor() {
    this(new ObjectMapper());
  }

  public IllegalContentProcessor(final ObjectMapper mapper) {
    this.mapper = mapper;
    try (final BufferedReader bf = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("regex.txt"))))) {
      pattern = Pattern.compile(bf.readLine());
    } catch (IOException e) {
      log.error("Failed to load regex pattern : {}", e.getMessage(), e);
    }
  }

  /**
   * returns true when content contains illegal/invalid content
   */
  @Override
  public boolean test(String s, ElectionCreate electionCreate) {
    try {
      final String json = mapper.writeValueAsString(electionCreate).toLowerCase();
      return pattern.matcher(json).find();
    } catch (JsonProcessingException e) {
      log.error("Error converting event into POJO : {}", electionCreate, e);
      return true;
    }
  }
}
