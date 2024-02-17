package io.voting.query.queryservice.controller;

import io.voting.common.library.models.EdvUser;
import io.voting.query.queryservice.payload.response.GenericResponse;
import io.voting.query.queryservice.payload.request.RegisterUserRequest;
import io.voting.query.queryservice.payload.response.JwtResponse;
import io.voting.query.queryservice.repository.EdvUserRepository;
import io.voting.query.queryservice.security.services.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  private TokenService tokenService;
  private EdvUserRepository repository;
  private PasswordEncoder passwordEncoder;

  private AuthController controller;

  @BeforeEach
  void setup() {
    tokenService = Mockito.mock(TokenService.class);
    repository = Mockito.mock(EdvUserRepository.class);
    passwordEncoder = Mockito.mock(PasswordEncoder.class);
    controller = new AuthController(tokenService, repository, passwordEncoder);
  }

  @Test
  void testRegisterUser() {
    final String username = "test";
    final String password = "password";
    final String secret = "secret";

    when(repository.findByUsername(username)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(password)).thenReturn(secret);
    when(repository.save(any())).thenReturn(EdvUser.builder().id("id").username(username).password(secret).build());

    final ResponseEntity<?> response = controller.registerUser(new RegisterUserRequest(username, password));

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo(new GenericResponse("User registered successfully!"));
  }

  @Test
  void testWhenUsernameAlreadyExists() {
    when(repository.findByUsername(anyString())).thenReturn(Optional.of(EdvUser.builder().build()));

    final ResponseEntity<?> response = controller.registerUser(new RegisterUserRequest("xyz", "xyz"));

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    assertThat(response.getBody()).isEqualTo(new GenericResponse("Error: Username is already taken!"));
  }

  @Test
  void testAuthenticateUser() {
    final Authentication authN = Mockito.mock(Authentication.class);
    final JwtResponse jwtResponse = new JwtResponse("", "xyz", "test");
    when(tokenService.generateJwtResponse(authN)).thenReturn(jwtResponse);

    final ResponseEntity<?> response = controller.authenticateUser(authN);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo(jwtResponse);
  }

}