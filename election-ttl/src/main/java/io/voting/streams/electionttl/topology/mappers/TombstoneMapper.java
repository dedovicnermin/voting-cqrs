package io.voting.streams.electionttl.topology.mappers;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

@Slf4j
public class TombstoneMapper implements KeyValueMapper<String, String, KeyValue<String, Void>> {

  @Override
  public KeyValue<String, Void> apply(String electionIdKey, String userIdValue) {
    final String newKey = electionIdKey + ":" + userIdValue;
    log.debug("Creating tombstone event for duplication vote cache key : {}", newKey);
    return KeyValue.pair(
            userIdValue + ":" + electionIdKey,
            null
    );
  }
}
