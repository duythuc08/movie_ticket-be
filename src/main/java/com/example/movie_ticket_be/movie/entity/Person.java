package com.example.movie_ticket_be.movie.entity;

import com.example.movie_ticket_be.movie.enums.MovieRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "movie_role")
    private MovieRole movieRole;
}
