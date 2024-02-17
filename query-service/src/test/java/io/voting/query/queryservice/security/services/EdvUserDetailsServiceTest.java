package io.voting.query.queryservice.security.services;

import io.voting.common.library.models.EdvUser;
import io.voting.query.queryservice.repository.EdvUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdvUserDetailsServiceTest {

  private EdvUserRepository repository;
  private EdvUserDetailsService service;

  @BeforeEach
  void setup() {
    repository = Mockito.mock(EdvUserRepository.class);
    service = new EdvUserDetailsService(repository);
  }

  @Test
  void testValidUsername() {
    final String username = "test";
    final EdvUser edvUser = EdvUser.builder().id("xyz")
            .username(username)
            .password("test")
            .build();

    when(repository.findByUsername(username)).thenReturn(Optional.of(edvUser));

    assertThat(service.loadUserByUsername(username))
            .isEqualTo(new EdvUserDetails(edvUser.getId(), edvUser.getUsername(), edvUser.getPassword()));
  }

  @Test
  void testNonExistentUser() {
    when(repository.findByUsername(anyString())).thenReturn(Optional.empty());
    Assertions.assertThrows(
            UsernameNotFoundException.class,
            () -> service.loadUserByUsername("test")
    );
  }

}