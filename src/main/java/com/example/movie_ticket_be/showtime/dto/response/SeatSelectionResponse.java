package com.example.movie_ticket_be.showtime.dto.response;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatSelectionResponse {
    List<SeatShowTimeResponse> seats;
    Map<SeatType, BigDecimal> pricingMap;
    List<SuggestedSeatResponse> suggested;
}
