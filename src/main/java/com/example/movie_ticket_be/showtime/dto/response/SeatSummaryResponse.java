package com.example.movie_ticket_be.showtime.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatSummaryResponse {
    long total;
    long available;
    long sold;
    long reserved;
    long blocked;
}
