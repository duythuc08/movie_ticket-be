package com.example.movie_ticket_be.promotion.dto.response;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.promotion.enums.EventStatus;
import com.example.movie_ticket_be.promotion.enums.EventType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventResponse {
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
