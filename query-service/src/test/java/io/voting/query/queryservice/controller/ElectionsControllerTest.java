package io.voting.query.queryservice.controller;

import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionStatus;
import io.voting.query.queryservice.payload.response.GenericResponse;
import io.voting.query.queryservice.repository.ElectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectionsControllerTest {

  private static final Election ELECTION = new Election("id", "author", "title", "desc", "category", Map.of("Foo", 1L, "Bar", 1L), 0L, 0L, ElectionStatus.OPEN);

  private ElectionRepository repository;
  private ElectionsController controller;

  @BeforeEach
  void setup() {
    repository = Mockito.mock(ElectionRepository.class);
    controller = new ElectionsController(repository);
  }

  @Test
  void testGetElections() {
    final List<Election> electionList = Arrays.asList(ELECTION, ELECTION, ELECTION);
    when(repository.findAll()).thenReturn(electionList);

    final ResponseEntity<?> response = controller.getElections();

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo(electionList);
  }

  @Test
  void testGetElectionsEmpty() {
    when(repository.findAll()).thenReturn(Collections.emptyList());

    final ResponseEntity<?> response = controller.getElections();

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo(Collections.emptyList());

  }

  @Test
  void testGetElectionsError() {
    doThrow(new RuntimeException("ON PURPOSE"))
            .when(repository).findAll();

    final ResponseEntity<?> response = controller.getElections();

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    assertThat(response.getBody()).isEqualTo(new GenericResponse("Error: " + "ON PURPOSE"));
  }

  @Test
  void testGetElection() {
    when(repository.findById(anyString())).thenReturn(Optional.of(ELECTION));

    final ResponseEntity<?> response = controller.getElection("000");

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    assertThat(response.getBody()).isEqualTo(ELECTION);
  }

  @Test
  void testGetElectionException() {
    doThrow(new RuntimeException("ON PURPOSE"))
            .when(repository).findById(anyString());

    final ResponseEntity<?> response = controller.getElection("000");

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    assertThat(response.getBody()).isEqualTo(new GenericResponse("Error: " + "ON PURPOSE"));
  }

  @Test
  void testGetNonExistentElection() {
    when(repository.findById(anyString())).thenReturn(Optional.empty());

    final ResponseEntity<?> response = controller.getElection("000");

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    assertThat(response.getBody()).isNull();

  }

}