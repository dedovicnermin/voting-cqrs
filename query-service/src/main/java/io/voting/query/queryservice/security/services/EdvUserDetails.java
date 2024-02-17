package io.voting.query.queryservice.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.voting.common.library.models.EdvUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EdvUserDetails implements UserDetails {

  private String id;
  private String username;
  @JsonIgnore
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public EdvUserDetails(String id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.authorities = Arrays.asList(new SimpleGrantedAuthority("read"), new SimpleGrantedAuthority("write"));
  }

  public static EdvUserDetails build(EdvUser user) {
    return new EdvUserDetails(user.getId(), user.getUsername(), user.getPassword());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
