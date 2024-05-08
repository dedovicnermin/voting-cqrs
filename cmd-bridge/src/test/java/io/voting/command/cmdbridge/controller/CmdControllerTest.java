package io.voting.command.cmdbridge.controller;

import io.voting.command.cmdbridge.config.AppConfig;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.rsocket.server.LocalRSocketServerPort;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Arrays;


@SpringBootTest
class CmdControllerTest {

  private static RSocketRequester requester;

  @BeforeAll
  public static void setupOnce(
          @Autowired RSocketRequester.Builder builder,
          @LocalRSocketServerPort Integer port,
          @Autowired RSocketStrategies strategies) {
    requester = builder
            .rsocketStrategies(strategies)
            .connectWebSocket(URI.create("ws://localhost:" + port + "/cmd"))
            .block();
  }

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
  }

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
  }


}