package com.example.movie_ticket_be.movie.dto.request;


import com.example.movie_ticket_be.movie.enums.BannerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerRequest {
    private String imageUrl;
    private String title;
    private String description;
    private String linkUrl;
    private Integer priority;
    private Boolean active;

    private Long movieId;
    private Long eventId;

    private BannerType bannerType;
}
