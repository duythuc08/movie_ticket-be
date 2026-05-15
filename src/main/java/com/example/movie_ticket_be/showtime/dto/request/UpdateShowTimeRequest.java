package com.example.movie_ticket_be.showtime.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateShowTimeRequest {
    LocalDateTime startTime;
    LocalDateTime endTime;
    Long movieId;
    Long roomId;
}