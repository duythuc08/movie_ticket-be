package com.example.movie_ticket_be.showtime.dto.response;

import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.movie.entity.Movies;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimeResponse {
    Long showTimeId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String showTimeStatus;

    Movies movies;
    Rooms rooms;
}
