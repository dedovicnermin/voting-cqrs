package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElectionViewCmdMapper implements CloudEventMapper<String, ElectionView> {

  @Override
  public CloudEvent apply(String key, ElectionView electionView) {
    log.trace("Applying transformation (K,V): {}, {}", key, electionView);
    final CloudEvent event = ceBuilder
            .withId(key)
            .withType(CloudEventTypes.ELECTION_VIEW_CMD)
            .withSubject(key)
            .withData(StreamUtils.wrapCloudEventData(electionView))
            .build();
    log.trace("Applied transformation: {}", event);
    return event;
  }
}
