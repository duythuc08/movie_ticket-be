package com.example.movie_ticket_be.cinema.entity;

import com.example.movie_ticket_be.cinema.enums.SeatStatus;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long seatId;
    String seatRow;
    Integer seatNumber;

    @ManyToOne
    @JoinColumn(name = "room_id")
    Rooms rooms;

    @Enumerated(EnumType.STRING)
    SeatType seatType;
    @Enumerated(EnumType.STRING)
    SeatStatus seatStatus;
}
