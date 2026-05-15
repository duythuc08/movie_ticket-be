package com.example.movie_ticket_be.showtime.dto.response;

import com.example.movie_ticket_be.cinema.enums.RoomType;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatShowTimeResponse {
    Long seatShowTimeId;

    String userId;

    Long seatId;
    String seatRow;
    Integer seatNumber;
    String seatType;

    Long showTimeId;
    Long roomId;
    String roomName;
    RoomType roomType;

    LocalDateTime lockedUntil;
    SeatShowTimeStatus seatShowTimeStatus;
}
