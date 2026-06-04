package com.example.movie_ticket_be.movie.dto.response;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.BannerType;
import com.example.movie_ticket_be.promotion.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {
	Long id;
	String imageUrl;
	String title;
	String description;
	String linkUrl;
	Integer priority;
	Boolean active;

	BannerType bannerType;

	Movies movies;
	Event event;
}
