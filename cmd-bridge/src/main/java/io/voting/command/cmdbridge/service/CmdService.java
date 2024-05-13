package io.voting.command.cmdbridge.service;

import io.cloudevents.CloudEvent;
import io.voting.command.cmdbridge.mappers.CloudEventMapper;
import io.voting.command.cmdbridge.mappers.ElectionCreateCmdMapper;
import io.voting.command.cmdbridge.mappers.ElectionViewCmdMapper;
import io.voting.command.cmdbridge.mappers.VoteCmdMapper;
import io.voting.common.library.kafka.clients.sender.EventSender;
import io.voting.common.library.models.ElectionCreate;
import io.voting.common.library.models.ElectionView;
import io.voting.common.library.models.ElectionVote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CmdService {

  private final EventSender<String, CloudEvent> voteSender;
  private final EventSender<String, CloudEvent> electionSender;
  private final CloudEventMapper<String, ElectionCreate> electionCreateMapper;
  private final CloudEventMapper<String, ElectionVote> voteMapper;
  private final CloudEventMapper<String, ElectionView> electionViewMapper;

  public CmdService(final EventSender<String, CloudEvent> voteSender, final EventSender<String, CloudEvent> electionSender) {
    this.electionSender = electionSender;
    this.voteSender = voteSender;
    this.electionCreateMapper = new ElectionCreateCmdMapper();
    this.voteMapper = new VoteCmdMapper();
    this.electionViewMapper = new ElectionViewCmdMapper();
  }

  public void handle(final String key, final ElectionVote vote) {
    voteSender.send(key, voteMapper.apply(key, vote));
  }

  public void handle(final String key, final ElectionCreate electionCreate) {
    electionSender.send(key, electionCreateMapper.apply(key, electionCreate));
  }

  public void handle(final String key, final ElectionView electionView) {
    electionSender.send(key, electionViewMapper.apply(key, electionView));
  }
}
