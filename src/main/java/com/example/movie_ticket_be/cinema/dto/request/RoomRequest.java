package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.enums.RoomStatus;
import com.example.movie_ticket_be.cinema.enums.RoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomRequest {
    String name;
    Integer capacity;

    @ManyToOne
    @JoinColumn(name = "cinema_id")
    Cinemas cinemas;

    @Enumerated(EnumType.STRING)
    RoomType roomType;

    @Enumerated(EnumType.STRING)
    RoomStatus roomStatus;
}
