package com.example.movie_ticket_be.recommendation.entity;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "user_preference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPreference {
    @EmbeddedId
    UserPreferenceId preferenceId;

    BigDecimal predictedScore;
    Integer neighborCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id",nullable = false)
    Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId")
    @JoinColumn(name = "movie_id",nullable = false)
    Movies movie;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPreferenceId implements Serializable {

        @Column(name = "user_id")
        String userId;

        @Column(name = "movie_id")
        Long movieId;

    }
}
