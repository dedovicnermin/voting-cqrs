package io.voting.persistence.eventsink.dao;

import com.github.javafaker.Faker;
import io.voting.common.library.models.Election;
import io.voting.common.library.models.ElectionVote;
import io.voting.persistence.eventsink.repository.ElectionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ElectionDaoImplTest {

  static final Faker fake = Faker.instance();

  /** Used for asserting */
  @Autowired
  private MongoTemplate template;

  /** Used for composing dao */
  @Autowired
  private ElectionRepository repository;
  private ElectionDao dao;

  @BeforeEach
  void setup() {
    dao = new ElectionDaoImpl(repository);
  }

  @AfterEach
  void cleanup() {
    template.getDb().drop();
  }

  @Test
  void testInsert() {
    final Election election = new Election(null, fake.funnyName().name(), fake.lordOfTheRings().location(), fake.lorem().sentence(), "TEST", Map.of("Foo", 0L, "Bar", 0L));
    final Election actual = dao.insertElection(election);

    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isNotNull();
    assertThat(actual.getAuthor()).isEqualTo(election.getAuthor());
    assertThat(actual.getTitle()).isEqualTo(election.getTitle());
    assertThat(actual.getDescription()).isEqualTo(election.getDescription());
    assertThat(actual.getCategory()).isEqualTo(election.getCategory());
    assertThat(actual.getCandidates()).isEqualTo(election.getCandidates());

    assertThat(template.findAll(Election.class).size()).isOne();
  }

  @Test
  void testUpdate() {
    final Election election = new Election(null, fake.funnyName().name(), fake.lordOfTheRings().location(), fake.lorem().sentence(), "TEST", Map.of("Foo", 0L, "Bar", 0L));
    final String id = template.insert(election).getId();

    dao.updateElection(new ElectionVote(id, "Foo"));
    assertThat(template.findById(id, Election.class).getCandidates())
            .containsEntry("Foo", 1L)
            .containsEntry("Bar", 0L);

    dao.updateElection(new ElectionVote(id, "Foo"));
    assertThat(template.findById(id, Election.class).getCandidates())
            .containsEntry("Foo", 2L)
            .containsEntry("Bar", 0L);

    dao.updateElection(new ElectionVote(id, "Bar"));
    assertThat(template.findById(id, Election.class).getCandidates())
            .containsEntry("Foo", 2L)
            .containsEntry("Bar", 1L);

    dao.updateElection(new ElectionVote(id, "Bar"));
    assertThat(template.findById(id, Election.class).getCandidates())
            .containsEntry("Foo", 2L)
            .containsEntry("Bar", 2L);
  }
}
