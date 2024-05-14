package io.voting.command.cmdbridge.config;

import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.StringDecoder;
import org.springframework.util.MimeType;

@Configuration
public class AppConfig {

  public static final String CMD_HEADER_ID = "key";
  public static final MimeType CMD_MIMETYPE = MimeType.valueOf("messaging/" + CMD_HEADER_ID);

  @Bean
  RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
    return strategies -> strategies
            .metadataExtractorRegistry(registry -> registry.metadataToExtract(CMD_MIMETYPE, String.class, CMD_HEADER_ID))
            .decoders(decoders -> decoders.add(StringDecoder.allMimeTypes()));
  }
}
