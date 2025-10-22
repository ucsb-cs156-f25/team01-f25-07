package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** This is a JPA entity that represents Articles */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "articles")
public class Articles {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String title;
  private String url;
  private String explanation;
  private String email;
  private LocalDateTime dateAdded;
}
