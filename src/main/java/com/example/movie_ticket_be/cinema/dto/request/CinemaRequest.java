package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CinemaRequest {
    String name;
    String address;
    @Column(unique = true)
    String phoneNumber;
    @Column(unique = true)
    String email;

    @Enumerated(EnumType.STRING)
    CinemaStatus cinemaStatus;
}
