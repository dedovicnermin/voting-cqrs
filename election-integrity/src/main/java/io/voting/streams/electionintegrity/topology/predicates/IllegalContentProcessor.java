package io.voting.streams.electionintegrity.topology.predicates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.voting.common.library.models.ElectionCreate;
import io.voting.events.cmd.CreateElection;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Predicate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
public class IllegalContentProcessor implements Predicate<String, CreateElection> {

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
  public boolean test(String s, CreateElection electionCreate) {
    try {
      final List<String> candidates = electionCreate.getCandidates().stream().map(CharSequence::toString).toList();
      ElectionCreate election = new ElectionCreate(electionCreate.getAuthor().toString(), electionCreate.getTitle().toString(), electionCreate.getDescription().toString(), electionCreate.getCategory().name(), candidates);
      final String json = mapper.writeValueAsString(election).toLowerCase();
      return pattern.matcher(json).find();
    } catch (JsonProcessingException e) {
      log.error("Error converting event into POJO : {}", electionCreate, e);
      return true;
    }
  }

  public static Named name() {
    return Named.as("ei.integrity.filter");
  }
}
