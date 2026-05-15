package com.example.movie_ticket_be.booking.dto.response;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderTicketResponse {
    Long orderTicketId;
    String seatName;
    String roomName;
    String movieName;
    String showTime;
    BigDecimal price;
    SeatType seatType;
}
