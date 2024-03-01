package io.voting.query.queryservice.repository;

import io.voting.common.library.models.EdvUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EdvUserRepository extends MongoRepository<EdvUser, String> {
  Optional<EdvUser> findByUsername(String username);
}
