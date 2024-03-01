package io.voting.query.queryservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.voting.common.library.models.EdvUser;
import io.voting.common.library.models.Election;
import io.voting.query.queryservice.payload.request.RegisterUserRequest;
import io.voting.query.queryservice.payload.response.JwtResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class QueryServiceApplicationTest {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final TestRestTemplate restTemplate = new TestRestTemplate();

  private static EdvUser user;
  private static Election election;

  @LocalServerPort
  private int port;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private PasswordEncoder encoder;

  @BeforeEach
  void setup() {
    user = mongoTemplate.insert(EdvUser.builder().username("test").password(encoder.encode("password")).build());
    election = mongoTemplate.insert(new Election(null, "author", "title", "desc", "category", Map.of("Foo", 0L, "Bar", 0L)));
  }

  @AfterEach
  void cleanup() {
    mongoTemplate.getDb().drop();
  }

  @Test
  void testRegisterUser() {
    final RegisterUserRequest registerUserRequest = new RegisterUserRequest("bob", "bob-secret");
    final HttpEntity<RegisterUserRequest> entity = new HttpEntity<>(registerUserRequest, new HttpHeaders());


    final ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/api/auth/register"), HttpMethod.POST, entity, String.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));

    final List<EdvUser> edvUsers = mongoTemplate
            .findAll(EdvUser.class)
            .stream()
            .filter(u -> !u.getId().equals(user.getId()))
            .collect(Collectors.toList());

    assertThat(edvUsers).hasSize(1);
    assertThat(edvUsers.get(0).getUsername()).isEqualTo(registerUserRequest.getUsername());
    assertThat(edvUsers.get(0).getPassword())
            .isNotEqualTo(registerUserRequest.getPassword());

  }

  @Test
  void testGetElections() throws JsonProcessingException {
    final HttpEntity<String> entity = new HttpEntity<>(null, getHeadersWithBearer());

    final ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/api/elections"), HttpMethod.GET, entity, String.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    final List<Election> body = mapper.readValue(response.getBody(), new TypeReference<>() {});
    assertThat(body).isEqualTo(Collections.singletonList(election));
  }

  @Test
  void testGetElection() {
    final HttpEntity<String> entity = new HttpEntity<>(null, getHeadersWithBearer());

    final ResponseEntity<Election> response = restTemplate
            .exchange(createURLWithPort("/api/elections/" + election.getId()), HttpMethod.GET, entity, Election.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo(election);
  }

  @Test
  void testHomeEndpoint() {
    final HttpEntity<String> entity = new HttpEntity<>(null, getHeadersWithBearer());

    final ResponseEntity<String> response = restTemplate
            .exchange(createURLWithPort("/home"), HttpMethod.GET, entity, String.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo("Hello, " + user.getUsername());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/elections", "/api/elections/xyz"})
  void unauthenticatedAccessNotAllowed(String uri) {
    final HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

    final ResponseEntity<String> response = restTemplate.exchange(createURLWithPort(uri), HttpMethod.GET, entity, String.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(401));
  }

  private String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }

  private HttpHeaders getHeadersWithBearer() {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + getToken());
    return headers;
  }

  private String getToken() {
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString("test:password".getBytes()));
    final ResponseEntity<JwtResponse> exchange = restTemplate
            .exchange(createURLWithPort("/api/auth/login"), HttpMethod.POST, new HttpEntity<>(null, headers), JwtResponse.class);
    return exchange.getBody().getToken();
  }

}