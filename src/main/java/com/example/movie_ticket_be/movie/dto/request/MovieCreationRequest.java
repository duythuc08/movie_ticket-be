package com.example.movie_ticket_be.movie.dto.request;

import com.example.movie_ticket_be.movie.enums.AgeRating;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieCreationRequest {
    String title;
    String description;
    Integer duration;
    String posterUrl;
    String trailerUrl;
    LocalDate releaseDate;

    private Set<Long> castIds;
    private Set<Long> directorIds;
    String language;
    String subTitle;

    Set<String> genreName;

    AgeRating ageRating;
    MovieStatus movieStatus;
}
