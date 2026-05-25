package com.example.movie_ticket_be.cinema.dto.response;

import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import com.example.movie_ticket_be.cinema.enums.RoomStatus;
import com.example.movie_ticket_be.cinema.enums.RoomType;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCinemaResponse {
    Long cinemaId;
    String name;
    String address;
    String phoneNumber;
    String email;
    CinemaStatus cinemaStatus;
    List<AdminRoomResponse> rooms;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdminRoomResponse {
        Long roomId;
        String name;
        Integer capacity;
        RoomType roomType;
        RoomStatus roomStatus;
    }
}
