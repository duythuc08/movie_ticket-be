package com.example.movie_ticket_be.movie.dto.response;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.enums.AgeRating;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    LocalDateTime releaseDate;

    Set<PersonResponse> castPersons;
    Set<PersonResponse> directors;
    String language;
    String subTitle;

    Set<GenreResponse> genre;

    AgeRating ageRating;
    MovieStatus movieStatus;
    EntityStatus entityStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
