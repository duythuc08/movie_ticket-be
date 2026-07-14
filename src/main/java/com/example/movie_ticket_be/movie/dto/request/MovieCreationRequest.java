package com.example.movie_ticket_be.movie.dto.request;

import com.example.movie_ticket_be.movie.enums.AgeRating;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
	@NotBlank
	String title;
	String description;
	@Min(value = 1, message = "Thời lượng phim phải ít nhất 1 phút")
	@Max(value = 480, message = "Thời lượng phim không được vượt quá 480 phút")
	Integer duration;
	String posterUrl;
	String trailerUrl;
	LocalDateTime releaseDate;

	Set<Long> castIds;
	Set<Long> directorIds;
	String language;
	String subTitle;

	@NotEmpty
	Set<String> genreName;

	AgeRating ageRating;
}
