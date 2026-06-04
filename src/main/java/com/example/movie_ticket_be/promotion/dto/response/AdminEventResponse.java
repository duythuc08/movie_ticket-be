package com.example.movie_ticket_be.promotion.dto.response;

import java.time.LocalDateTime;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.promotion.enums.EventStatus;
import com.example.movie_ticket_be.promotion.enums.EventType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminEventResponse {
    Long eventId;
    String title;
    String description;
    String posterUrl;
    LocalDateTime startTime;
    LocalDateTime endTime;
    EventType eventType;
    EventStatus eventStatus;
    EntityStatus entityStatus;
    Long movieId;
}
