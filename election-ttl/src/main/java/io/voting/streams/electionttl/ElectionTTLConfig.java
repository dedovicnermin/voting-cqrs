package io.voting.streams.electionttl;

public final class ElectionTTLConfig {

  private ElectionTTLConfig() {}

  public static final String ELECTION_REQUESTS_TOPIC = "election.requests.topic";
  public static final String ELECTION_VOTES_TOPIC = "election.votes.topic";
  public static final String ELECTION_TTL = "election.ttl";
  public static final String VOTE_INTEGRITY_CHANGELOG_TOPIC = "vote.integrity.changelog.topic";

}
