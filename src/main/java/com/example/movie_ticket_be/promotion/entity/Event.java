package com.example.movie_ticket_be.promotion.entity;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.promotion.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long eventId;

    String title;
    @Lob
    String description;
    String posterUrl;
    LocalDateTime startTime;
    LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    EventType eventType;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    Movies movies;
}
