package io.voting.command.cmdbridge.controller;

import io.cloudevents.CloudEvent;
import io.voting.command.cmdbridge.config.AppConfig;
import io.voting.command.cmdbridge.controller.framework.TestConsumerHelper;
import io.voting.command.cmdbridge.controller.framework.TestKafkaContext;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.CreateElection;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.enum$.ElectionCategory;
import lombok.SneakyThrows;
import org.apache.avro.generic.IndexedRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class CmdControllerTest extends TestKafkaContext {

  private static TestConsumerHelper consumerHelper;
  private static RSocketRequester requester;


  @DynamicPropertySource
  static void registerKafkaProperties(final DynamicPropertyRegistry dynamicPropertyRegistry) {
    dynamicPropertyRegistry.add("kafka.properties.bootstrap.servers", kafkaContainer::getBootstrapServers);
    dynamicPropertyRegistry.add("kafka.properties.schema.registry.url", TestKafkaContext::schemaRegistryUrl);
  }

  @BeforeAll
  public static void setupOnce(
          @Autowired RSocketRequester.Builder builder,
          @LocalRSocketServerPort Integer port,
          @Autowired RSocketStrategies strategies) {
    requester = builder
            .rsocketStrategies(strategies)
            .connectWebSocket(URI.create("ws://localhost:" + port + "/cmd"))
            .block();
    consumerHelper = new TestConsumerHelper(kafkaContainer);
  }

  @AfterEach
  void cleanup() {
    consumerHelper.clearQueues();
  }

  @SneakyThrows
  @Test
  void testVoteCmd() {
    final Mono<Void> result = requester
            .route("new-vote")
            .metadata("777:888", AppConfig.CMD_MIMETYPE)
            .data(new ElectionVote("888", "Doug"))
            .retrieveMono(Void.class);

    StepVerifier
            .create(result)
            .verifyComplete();
    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(10, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("777:888");
    assertThat(element.getPOrE().getError()).isNull();
    final CloudEvent payload = element.getPOrE().getPayload();
    assertThat(payload.getType()).isEqualTo(RegisterVote.class.getName());

    final CmdEvent payloadData = AvroCloudEventData.dataOf(payload.getData());

    assertThat(payloadData.getCmd()).isNotNull();
    assertThat(((RegisterVote)payloadData.getCmd()).getEId().toString()).isEqualTo("888");
    assertThat(((RegisterVote)payloadData.getCmd()).getVotedFor().toString()).isEqualTo("Doug");

  }

  @SneakyThrows
  @Test
  void testElectionCmd() {
    final ElectionCreate expectedCmdData = new ElectionCreate("testAuthor", "testTitle", "testDesc", "Gaming", Arrays.asList("Doug", "Carter", "testUser"));
    final Mono<Void> result = requester
            .route("new-election")
            .metadata("777", AppConfig.CMD_MIMETYPE)
            .data(expectedCmdData)
            .retrieveMono(Void.class);

    StepVerifier
            .create(result)
            .verifyComplete();

    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(10, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("777");
    assertThat(element.getPOrE().getPayload().getType()).isEqualTo(CreateElection.class.getName());
    final CmdEvent payloadData = AvroCloudEventData.dataOf(element.getPOrE().getPayload().getData());

    assertThat(payloadData.getCmd()).isNotNull().isInstanceOf(CreateElection.class);
    final CreateElection actualPayloadData = (CreateElection) payloadData.getCmd();
    assertThat(actualPayloadData.getAuthor().toString()).isEqualTo(expectedCmdData.getAuthor());
    assertThat(actualPayloadData.getTitle().toString()).isEqualTo(expectedCmdData.getTitle());
    assertThat(actualPayloadData.getDescription().toString()).isEqualTo(expectedCmdData.getDescription());
    assertThat(actualPayloadData.getCandidates()).isEqualTo(new ArrayList<>(expectedCmdData.getCandidates()));
    assertThat(actualPayloadData.getCategory()).isEqualTo(ElectionCategory.Gaming);
  }

  @SneakyThrows
  @Test
  void testElectionViewCmd() {
    final Mono<Void> result = requester
            .route("new-view")
            .metadata("778", AppConfig.CMD_MIMETYPE)
            .data(ElectionView.OPEN.toString())
            .retrieveMono(Void.class);

    StepVerifier
            .create(result)
            .verifyComplete();

    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(3, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("778");
    final CloudEvent payload = element.getPOrE().getPayload();
    assertThat(payload.getType()).isEqualTo(CloudEventTypes.ELECTION_VIEW_CMD);
    assertThat(payload.getSubject()).isEqualTo("778");
    assertThat(StreamUtils.unwrapCloudEventData(payload.getData(), ElectionView.class)).isEqualTo(ElectionView.OPEN);

  }
}
