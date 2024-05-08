package io.voting.common.library.kafka.utils;

public final class CloudEventTypes {

  private CloudEventTypes() {}

  public static final String ELECTION_VOTE_CMD = "ELECTION_VOTE_CMD";
  public static final String ELECTION_VOTE_EVENT = "ELECTION_VOTE_EVENT";
  public static final String ELECTION_CREATE_EVENT = "ELECTION_CREATE_EVENT";
  public static final String ELECTION_CREATE_CMD = "ELECTION_CREATE_CMD";
  public static final String ELECTION_EXPIRATION_EVENT = "ELECTION_EXPIRATION_EVENT";
}
