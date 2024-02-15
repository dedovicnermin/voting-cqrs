package io.voting.common.library.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("users")
public class EdvUser {

  @Id
  private String id;
  @Indexed
  private String username;
  private String password;
  private boolean active;

}
