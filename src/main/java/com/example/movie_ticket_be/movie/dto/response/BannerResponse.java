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
    private Long id;
    private String imageUrl;
    private String title;
    private String description;
    private String linkUrl;
    private Integer priority;
    private Boolean active;

    private BannerType bannerType;

    Movies movies;
    Event event;
}
