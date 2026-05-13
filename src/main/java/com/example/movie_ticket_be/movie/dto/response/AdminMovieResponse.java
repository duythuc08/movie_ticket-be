package com.example.movie_ticket_be.movie.dto.response;

import com.example.movie_ticket_be.movie.enums.AgeRating;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminMovieResponse {
    Long movieId;
    String title;
    String description;
    Integer duration;
    String posterUrl;
    String trailerUrl;
    LocalDate releaseDate;

    private Set<PersonResponse> castPersons;
    private Set<PersonResponse> directors;
    String language;
    String subTitle;

    Set<GenreResponse> genre;

    AgeRating ageRating;
    MovieStatus movieStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
