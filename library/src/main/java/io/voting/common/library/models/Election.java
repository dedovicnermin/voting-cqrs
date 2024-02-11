package io.voting.common.library.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Election {

  @Id
  private String id;
  @Indexed
  private String author;
  private String title;
  private String description;
  @Indexed
  private String category;
  private Map<String, Long> candidates;

}
