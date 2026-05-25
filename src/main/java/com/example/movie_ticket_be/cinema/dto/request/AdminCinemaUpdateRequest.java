package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCinemaUpdateRequest {
    String name;
    String address;
    String phoneNumber;
    String email;
    CinemaStatus cinemaStatus;
    List<AdminRoomRequest> rooms;
}
