package io.voting.query.queryservice.controller;

import io.voting.query.queryservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final TokenService tokenService;

  @PostMapping("/token")
  public String token(Authentication authentication) {
    log.debug("Token requested for user: '{}", authentication.getName());
    final String token = tokenService.generateToken(authentication);
    log.debug("Token granted {}", token);
    return token;
  }

}
