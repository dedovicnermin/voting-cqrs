package io.voting.command.cmdbridge.controller;

import io.cloudevents.CloudEvent;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
public class CmdController {

  private final EventSender<String, CloudEvent> voteSender;
  private final EventSender<String, CloudEvent> electionSender;

  public CmdController(EventSender<String, CloudEvent> voteSender, EventSender<String, CloudEvent> electionSender) {
    this.voteSender = voteSender;
    this.electionSender = electionSender;
  }

  /**
   * Listen to new vote commands send from frontend
   * @param key userId:electionId
   * @param vote election vote
   * @return nothing / fire-and-forget
   */
  @MessageMapping({"new-vote"})
  public Mono<Void> voteCommandTest(@Header String key, @Payload final ElectionVote vote) {
    log.info("Voter with ID ({}) requested vote: {}", key, vote);
    return Mono.empty();
  }

  /**
   * Listen to new election commands send from frontend
   * @param key userId
   * @param election new election
   * @return nothing / fire-and-forget
   */
  @MessageMapping("new-election")
  public Mono<Void> electionCommand(@Header String key, @Payload final ElectionCreate election) {
    log.info("Received election requested by ({}): {}", key, election);
    return Mono.empty();
  }

}
