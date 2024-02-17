package io.voting.query.queryservice.controller;

import io.voting.common.library.models.Election;
import io.voting.query.queryservice.payload.response.GenericResponse;
import io.voting.query.queryservice.repository.ElectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ElectionsController {

  private final ElectionRepository electionRepository;

  @GetMapping("/api/elections")
  public ResponseEntity<?> getElections() {
    try {
      final List<Election> elections = electionRepository.findAll();
      return ResponseEntity.ok(elections);
    } catch (Exception e) {
      log.error("Unexpected exception occurred : {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().body(new GenericResponse("Error: " + e.getMessage()));
    }
  }

  @GetMapping("/api/elections/{id}")
  public ResponseEntity<?> getElection(@PathVariable("id") String id) {
    try {
      return electionRepository.findById(id)
              .map(election -> ResponseEntity.ok().body(election))
              .orElse(ResponseEntity.badRequest().body(null));
    } catch (Exception e) {
      log.debug("Unexpected exception occurred : {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().body(new GenericResponse("Error: " + e.getMessage()));
    }
  }

}
