package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionCreate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElectionCmdMapper implements CloudEventMapper<String, ElectionCreate> {

  @Override
  public CloudEvent apply(String key, ElectionCreate electionCreate) {
    log.trace("Applying transformation (K,V): {}, {}", key, electionCreate);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(CloudEventTypes.ELECTION_CREATE_CMD)
            .withSubject(electionCreate.getCategory())
            .withData(StreamUtils.wrapCloudEventData(electionCreate))
            .build();
    log.trace("Applied transformation: {}", event);
    return event;
  }

}
