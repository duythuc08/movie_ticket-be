package com.example.movie_ticket_be.movie.entity;

import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reviews extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long reviewId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Users users;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    Movies movies;

    Integer rating;
    String comment;
    Integer likeCount;

    @Enumerated(EnumType.STRING)
    ReviewStatus reviewStatus;
}
