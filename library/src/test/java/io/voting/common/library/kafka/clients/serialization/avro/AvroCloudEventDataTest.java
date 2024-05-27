package io.voting.common.library.kafka.clients.serialization.avro;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;
import io.voting.events.enums.ElectionView;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AvroCloudEventDataTest {

  @Test
  void testGetValue() {
    final CmdEvent cmdEvent = new CmdEvent(new RegisterVote("eid", "FooBar"));
    final AvroCloudEventData<CmdEvent> ceData = new AvroCloudEventData<>(cmdEvent);
    assertEquals(cmdEvent, ceData.getValue());
  }

  @Test
  void testWrappedValue() {
    final CmdEvent cmdEvent = new CmdEvent(new ViewElection("eid", ElectionView.OPEN));
    final AvroCloudEventData<CmdEvent> ceData = new AvroCloudEventData<>(cmdEvent);

    final CloudEvent ce = CloudEventBuilder.v1()
            .withId("")
            .withSource(URI.create("/test"))
            .withType(cmdEvent.getCmd().getClass().getName())
            .withSubject("TEST")
            .withTime(OffsetDateTime.now())
            .withData(AvroCloudEventData.MIME_TYPE, ceData)
            .build();

    assertEquals(ceData, ce.getData());
  }



}