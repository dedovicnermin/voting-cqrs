package io.voting.streams.voteintegrity.config;

public final class Constants {

  private Constants() {}

  public static final String INPUT_TOPIC_CONFIG = "input.topic";
  public static final String INPUT_TOPIC_CONFIG_DEFAULT = "election.votes.raw";

  public static final String OUTPUT_TOPIC_CONFIG = "output.topic";
  public static final String OUTPUT_TOPIC_CONFIG_DEFAULT = "election.votes";

}
