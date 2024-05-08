package io.voting.command.cmdbridge.service;

import io.cloudevents.CloudEvent;
import io.voting.command.cmdbridge.mappers.CloudEventMapper;
import io.voting.command.cmdbridge.mappers.ElectionCmdMapper;
import io.voting.command.cmdbridge.mappers.VoteCmdMapper;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionVote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CmdService {

  private final EventSender<String, CloudEvent> voteSender;
  private final EventSender<String, CloudEvent> electionSender;
  private final CloudEventMapper<String, ElectionCreate> electionMapper;
  private final CloudEventMapper<String, ElectionVote> voteMapper;

  public CmdService(final EventSender<String, CloudEvent> voteSender, final EventSender<String, CloudEvent> electionSender) {
    this.electionSender = electionSender;
    this.electionMapper = new ElectionCmdMapper();
    this.voteSender = voteSender;
    this.voteMapper = new VoteCmdMapper();
  }

  public void handle(final String key, final ElectionVote vote) {
    voteSender.send(key, voteMapper.apply(key, vote));
  }

  public void handle(final String key, final ElectionCreate electionCreate) {
    electionSender.send(key, electionMapper.apply(key, electionCreate));
  }
}
