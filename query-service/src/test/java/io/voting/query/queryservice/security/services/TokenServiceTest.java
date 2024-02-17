package io.voting.query.queryservice.security.services;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import io.voting.query.queryservice.payload.response.JwtResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  private static JwtEncoder encoder;
  private static JwtDecoder decoder;

  private Authentication authentication;
  private TokenService service;

  @BeforeAll
  static void init() throws NoSuchAlgorithmException {
    final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    final KeyPair kp = generator.generateKeyPair();
    RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();
    final JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
    final JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    encoder = new NimbusJwtEncoder(jwks);
    decoder = NimbusJwtDecoder
            .withPublicKey(publicKey)
            .build();
  }

  @BeforeEach
  void setup() {
    authentication = Mockito.mock(Authentication.class);
    service = new TokenService(encoder);
  }

  @Test
  void test() {
    final EdvUserDetails user = new EdvUserDetails("xyz", "test", "password");
    when(authentication.getPrincipal()).thenReturn(user);
    when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

    final JwtResponse jwtResponse = service.generateJwtResponse(authentication);

    assertThat(jwtResponse).isNotNull();
    assertThat(jwtResponse.getId()).isEqualTo(user.getId());
    assertThat(jwtResponse.getUsername()).isEqualTo(user.getUsername());

    final Jwt decoded = decoder.decode(jwtResponse.getToken());
    assertThat(decoded.getSubject()).isEqualTo(user.getUsername());
    assertThat(decoded.getClaims()).containsKeys("exp", "iat", "iss", "scope", "sub");
    assertThat(decoded.getClaims())
            .containsEntry("iss", "self")
            .containsEntry("scope", "")
            .containsEntry("sub", user.getUsername());
  }

}