package com.example.movie_ticket_be.recommendation.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.example.movie_ticket_be.core.entity.BaseEntity;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.user.entity.Users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "utility_matrix")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UtilityMatrix extends BaseEntity {

    @EmbeddedId
    UtilityMatrixId matrixId;

    @Column(name = "y_score", precision = 4, scale = 2)
    BigDecimal yScore;

    @Column(name = "has_explicit", nullable = false)
    Boolean hasExplicit;

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
    public static class UtilityMatrixId implements Serializable {

        @Column(name = "user_id")
        private String userId;

        @Column(name = "movie_id")
        private Long movieId;
    }
}