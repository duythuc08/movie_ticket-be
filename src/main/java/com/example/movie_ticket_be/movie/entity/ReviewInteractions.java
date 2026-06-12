package com.example.movie_ticket_be.movie.entity;

import com.example.movie_ticket_be.core.entity.BaseEntity;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.enums.InteractionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review_interactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "review_id" }) })
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewInteractions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long reviewInteractionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Users users;

    @ManyToOne
    @JoinColumn(name = "review_id")
    Reviews reviews;

    @Enumerated(EnumType.STRING)
    InteractionType interactionType;

    boolean isActive = true;
}
