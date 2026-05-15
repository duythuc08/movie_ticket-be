package com.example.movie_ticket_be.showtime.dto.request;

import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatShowTimeRequest {
    LocalDateTime lockedUntil;
    String userId;
    Long seatId;
    Long showTimeId;
    SeatShowTimeStatus seatShowTimeStatus;
}
