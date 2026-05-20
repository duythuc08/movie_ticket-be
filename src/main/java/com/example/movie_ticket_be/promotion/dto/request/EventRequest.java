package com.example.movie_ticket_be.promotion.dto.request;

import com.example.movie_ticket_be.promotion.enums.EventType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequest {
    String title;
    String description;
    String posterUrl;
    LocalDateTime startTime;
    LocalDateTime endTime;
    EventType eventType;
    Long movieId;
}
