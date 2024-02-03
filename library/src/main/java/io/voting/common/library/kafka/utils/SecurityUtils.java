package io.voting.common.library.kafka.utils;

import org.apache.kafka.common.security.plain.PlainLoginModule;

import java.text.MessageFormat;

public final class SecurityUtils {

  private static final String JAAS_TEMPLATE = PlainLoginModule.class.getName() + " required username=\"{0}\" password=\"{1}\";";

  private SecurityUtils() {}

  public static String formatSaslJaasConfig(final String username, final String password) {
    return MessageFormat.format(JAAS_TEMPLATE, username, password);
  }


}
