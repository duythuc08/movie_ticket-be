package com.example.movie_ticket_be.movie.dto.request;

import com.example.movie_ticket_be.movie.enums.AgeRating;
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
public class MovieCreationRequest {
	String title;
	String description;
	Integer duration;
	String posterUrl;
	String trailerUrl;
	LocalDateTime releaseDate;

	Set<Long> castIds;
	Set<Long> directorIds;
	String language;
	String subTitle;

	Set<String> genreName;

	AgeRating ageRating;
}
