package io.voting.query.queryservice.service;

import io.voting.query.queryservice.repository.EdvUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdvUserDetailsService implements UserDetailsService {

  private final EdvUserRepository repository;
  private final PasswordEncoder encoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return repository.findByUsername(username)
            .map(edvUser -> User.builder()
                    .username(edvUser.getUsername())
                    .password(edvUser.getPassword())
                    .disabled(!edvUser.isActive())
                    .passwordEncoder(encoder::encode)
                    .authorities(edvUser.getId())
                    .build()
            ).orElseThrow(() -> new UsernameNotFoundException(username));
  }
}
