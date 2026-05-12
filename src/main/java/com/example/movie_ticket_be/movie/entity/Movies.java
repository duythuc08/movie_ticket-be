package com.example.movie_ticket_be.movie.entity;

import com.example.movie_ticket_be.movie.enums.AgeRating;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Movies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long movieId;

    String title;
    @Lob
    String description;
    Integer duration;
    String posterUrl;
    String trailerUrl;
    LocalDate releaseDate;
    String language;
    String subTitle;

    @ManyToMany
    @JoinTable(
            name = "movie_cast",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> castPersons = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "movie_directors",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> directors = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    Set<Genre> genre;

    @Enumerated(EnumType.STRING)
    AgeRating ageRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "movie_status")
    MovieStatus movieStatus;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
