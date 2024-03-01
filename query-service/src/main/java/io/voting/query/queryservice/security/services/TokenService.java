package io.voting.query.queryservice.security.services;

import io.voting.query.queryservice.payload.response.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {

  private final JwtEncoder jwtEncoder;

  public JwtResponse generateJwtResponse(final Authentication authentication) {
    final EdvUserDetails userPrincipal = (EdvUserDetails) authentication.getPrincipal();
    final Instant now = Instant.now();
    final String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));
    final JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .subject(userPrincipal.getUsername())
            .claim("scope", scope)
            .build();
    final String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    return new JwtResponse(token, userPrincipal.getId(), userPrincipal.getUsername());
  }
}
