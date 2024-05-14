package io.voting.streams.electionintegrity.topology.mappers;

import io.voting.streams.electionintegrity.model.ElectionSummary;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import java.util.Optional;

@Slf4j
public class ElectionIdExtractor implements KeyValueMapper<String, ElectionSummary, String> {

  /** Key expected format = 'userId:electionId' */
  @Override
  public String apply(String key, ElectionSummary electionSummary) {
    log.trace("Extracting election ID from key : {}", key);
    return Optional.ofNullable(key)
            .map(k -> k.split(":"))
            .filter(array -> array.length >= 2)
            .map(array -> array[1])
            .orElse(electionSummary.getVote().getElectionId());
  }
}
