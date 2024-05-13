package io.voting.command.cmdbridge.controller;

import io.cloudevents.CloudEvent;
import io.voting.command.cmdbridge.config.AppConfig;
import io.voting.command.cmdbridge.controller.framework.TestConsumerHelper;
import io.voting.command.cmdbridge.controller.framework.TestKafkaContext;
import io.voting.common.library.kafka.models.ReceiveEvent;
import io.voting.common.library.kafka.utils.CloudEventTypes;
import io.voting.common.library.kafka.utils.StreamUtils;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import lombok.SneakyThrows;
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
    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(3, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("777:888");
    assertThat(element.getPOrE().getPayload().getType()).isEqualTo(CloudEventTypes.ELECTION_VOTE_CMD);
  }

  @SneakyThrows
  @Test
  void testElectionCmd() {
    final Mono<Void> result = requester
            .route("new-election")
            .metadata("777", AppConfig.CMD_MIMETYPE)
            .data(new ElectionCreate("testAuthor", "testTitle", "testDesc", "TEST", Arrays.asList("Doug", "Carter", "testUser")))
            .retrieveMono(Void.class);

    StepVerifier
            .create(result)
            .verifyComplete();

    final ReceiveEvent<String, CloudEvent> element = consumerHelper.getEvents().poll(3, TimeUnit.SECONDS);
    assertThat(element.getKey()).isEqualTo("777");
    assertThat(element.getPOrE().getPayload().getType()).isEqualTo(CloudEventTypes.ELECTION_CREATE_CMD);
  }

  @SneakyThrows
  @Test
  void testElectionViewCmd() {
    final Mono<Void> result = requester
            .route("new-view")
            .metadata("778", AppConfig.CMD_MIMETYPE)
            .data(ElectionView.OPEN)
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
