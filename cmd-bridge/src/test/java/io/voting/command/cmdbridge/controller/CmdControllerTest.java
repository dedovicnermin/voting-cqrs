package io.voting.command.cmdbridge.controller;

import io.cloudevents.CloudEvent;
import io.voting.command.cmdbridge.config.AppConfig;
import io.voting.command.cmdbridge.controller.framework.TestConsumerHelper;
import io.voting.command.cmdbridge.controller.framework.TestKafkaContext;
import io.voting.common.library.kafka.clients.serialization.avro.AvroCloudEventData;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import io.voting.events.cmd.CmdEvent;
import io.voting.events.cmd.CreateElection;
import io.voting.events.cmd.RegisterVote;
import io.voting.events.cmd.ViewElection;
import io.voting.events.enums.ElectionCategory;
import lombok.SneakyThrows;
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
import org.springframework.web.client.RestTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.*;
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
    registerSchemas();
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

    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(5, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("777:888");
    assertThat(element.getPOrE().getError()).isNull();
    assertThat(element.getPOrE().getPayload().getType()).isEqualTo(RegisterVote.class.getName());

    final CmdEvent payloadData = AvroCloudEventData.dataOf(element.getPOrE().getPayload().getData());
    assertThat(payloadData.getCmd()).isNotNull().isInstanceOf(RegisterVote.class);

    final RegisterVote actualCmd = (RegisterVote) payloadData.getCmd();
    assertThat(actualCmd.getEId()).isEqualTo("888");
    assertThat(actualCmd.getVotedFor()).isEqualTo("Doug");

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

    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(5, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("777");
    assertThat(element.getPOrE().getPayload().getType()).isEqualTo(CreateElection.class.getName());
    final CmdEvent payloadData = AvroCloudEventData.dataOf(element.getPOrE().getPayload().getData());

    assertThat(payloadData.getCmd()).isNotNull().isInstanceOf(CreateElection.class);
    final CreateElection actualPayloadData = (CreateElection) payloadData.getCmd();
    assertThat(actualPayloadData.getAuthor()).isEqualTo(expectedCmdData.getAuthor());
    assertThat(actualPayloadData.getTitle()).isEqualTo(expectedCmdData.getTitle());
    assertThat(actualPayloadData.getDescription()).isEqualTo(expectedCmdData.getDescription());
    assertThat(actualPayloadData.getCategory()).isEqualTo(ElectionCategory.Gaming);
    assertThat(actualPayloadData.getCandidates()).isEqualTo(expectedCmdData.getCandidates());
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

    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(5, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("778");
    assertThat(element.getPOrE().getError()).isNull();
    assertThat(element.getPOrE().getPayload().getType()).isEqualTo(ViewElection.class.getName());
    assertThat(element.getPOrE().getPayload().getSubject()).isEqualTo("778");

    final CmdEvent cmdEvent = AvroCloudEventData.dataOf(element.getPOrE().getPayload().getData());
    assertThat(cmdEvent.getCmd()).isNotNull().isInstanceOf(ViewElection.class);

    final ViewElection actualData = (ViewElection) cmdEvent.getCmd();
    assertThat(actualData.getEId()).isEqualTo("778");
    assertThat(actualData.getView()).isEqualTo(io.voting.events.enums.ElectionView.OPEN);

    RestTemplate restTemplate = new RestTemplate();
    String s = restTemplate.getForObject(TestKafkaContext.schemaRegistryUrl() + "/schemas", String.class);
    System.out.println(s);
    ObjectMapper objectMapper = new ObjectMapper();
    System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(s)));

  }
}
