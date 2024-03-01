package io.voting.query.queryservice.security.services;

import io.voting.query.queryservice.repository.EdvUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdvUserDetailsService implements UserDetailsService {

  private final EdvUserRepository repository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("loadUserByUsername() invoked : {}", username);
    final EdvUserDetails edvUserDetails = repository.findByUsername(username)
            .map(EdvUserDetails::build)
            .orElseThrow(() -> new UsernameNotFoundException(username));
    log.debug("loadUserByUsername() result : {}", edvUserDetails);
    return edvUserDetails;
  }
}
