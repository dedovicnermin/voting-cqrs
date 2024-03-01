package io.voting.query.queryservice.controller;

import io.voting.common.library.models.EdvUser;
import io.voting.query.queryservice.payload.response.GenericResponse;
import io.voting.query.queryservice.payload.request.RegisterUserRequest;
import io.voting.query.queryservice.payload.response.JwtResponse;
import io.voting.query.queryservice.repository.EdvUserRepository;
import io.voting.query.queryservice.security.services.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final TokenService tokenService;
  private final EdvUserRepository repository;
  private final PasswordEncoder encoder;

  @PostMapping("/api/auth/login")
  public ResponseEntity<?> authenticateUser(Authentication authentication) {
    log.debug("Token requested for user: '{}'", authentication.getName());
    final JwtResponse token = tokenService.generateJwtResponse(authentication);
    log.trace("Token granted {}", token);
    return ResponseEntity.ok(token);
  }

  @PostMapping("/api/auth/register")
  public ResponseEntity<?> registerUser(@RequestBody final RegisterUserRequest request) {
    log.debug("Processing register user request for user : {}", request.getUsername());
    return repository.findByUsername(request.getUsername())
            .map(user -> ResponseEntity.badRequest().body(new GenericResponse("Error: Username is already taken!")))
            .orElseGet(() -> {
              final EdvUser edvUser = EdvUser.builder()
                      .username(request.getUsername())
                      .password(encoder.encode(request.getPassword()))
                      .build();
              repository.save(edvUser);
              return ResponseEntity.ok(new GenericResponse("User registered successfully!"));
            });
  }

}
