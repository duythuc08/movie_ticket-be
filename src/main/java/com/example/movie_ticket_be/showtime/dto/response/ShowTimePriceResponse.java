package com.example.movie_ticket_be.showtime.dto.response;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimePriceResponse {
    Long seatShowTimePriceId;
    Long showTimeId;
    SeatType seatType;
    BigDecimal price;
}
