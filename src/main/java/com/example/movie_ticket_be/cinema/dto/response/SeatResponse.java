package com.example.movie_ticket_be.cinema.dto.response;

import java.math.BigDecimal;

import com.example.movie_ticket_be.cinema.enums.SeatStatus;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.enums.EntityStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatResponse {
    Long seatId;
    String seatRow;
    Integer seatNumber;
    Long roomId;
    SeatType seatType;
    SeatStatus seatStatus;
    EntityStatus entityStatus;
    BigDecimal viewQuanlityScore; 
}
