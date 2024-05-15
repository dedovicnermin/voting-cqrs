package io.voting.command.cmdbridge.mappers;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.voting.command.cmdbridge.CmdBridgeApplication;

import java.net.URI;

public interface CloudEventMapper<K, V> {

  CloudEventBuilder ceBuilder = new CloudEventBuilder()
          .newBuilder()
          .withSource(URI.create("https://"+ CmdBridgeApplication.class.getSimpleName()));

  CloudEvent apply(K key, V value);

}
