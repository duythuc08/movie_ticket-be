package com.example.movie_ticket_be.movie.entity;

import com.example.movie_ticket_be.movie.enums.BannerType;
import com.example.movie_ticket_be.promotion.entity.Event;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;
    private String title;
    private String description;
    private String linkUrl;
    private Integer priority;
    private Boolean active;

    @Enumerated(EnumType.STRING)
    BannerType bannerType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id")
    private Movies movies;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;
}
