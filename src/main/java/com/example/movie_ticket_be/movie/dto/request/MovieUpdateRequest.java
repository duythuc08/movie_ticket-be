package com.example.movie_ticket_be.movie.dto.request;

import java.time.LocalDateTime;
import java.util.Set;

import com.example.movie_ticket_be.movie.enums.AgeRating;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieUpdateRequest {
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
