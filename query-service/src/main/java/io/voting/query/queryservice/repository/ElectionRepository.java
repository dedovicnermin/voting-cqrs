package io.voting.query.queryservice.repository;

import io.voting.common.library.models.Election;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectionRepository extends MongoRepository<Election, String> {
  List<Election> findAllByAuthor(String author);
}
