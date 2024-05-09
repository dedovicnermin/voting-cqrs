package io.voting.command.cmdbridge.controller;

import io.voting.command.cmdbridge.service.CmdService;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Controller
public class CmdController {

  private final CmdService service;

  public CmdController(final CmdService service) {
    this.service = service;
  }

  /**
   * Listen to new vote commands send from frontend
   * @param key userId:electionId
   * @param vote election vote
   * @return nothing / fire-and-forget
   */
  @MessageMapping({"new-vote"})
  public Mono<Void> voteCommandTest(@Header String key, @Payload final ElectionVote vote, @Headers Map<String, Object> headers) {
    log.info("Voter with ID ({}) requested vote: {}", key, vote);
    service.handle(key, vote);
    return Mono.empty();
  }

  /**
   * Listen to new election commands send from frontend
   * @param key userId
   * @param election new election
   * @return nothing / fire-and-forget
   */
  @MessageMapping("new-election")
  public Mono<Void> electionCommand(@Header String key, @Payload final ElectionCreate election, @Headers Map<String, Object> headers) {
    log.info("Received election requested by ({}): {}", key, election);
    service.handle(key, election);
    return Mono.empty();
  }

}
