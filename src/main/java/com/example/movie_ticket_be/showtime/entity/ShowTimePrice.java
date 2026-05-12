package com.example.movie_ticket_be.showtime.entity;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long showTimePriceId;

    @ManyToOne
    @JoinColumn(name = "show_time_id")
    ShowTimes showtimes;

    @Enumerated(EnumType.STRING)
    SeatType seatType;

    BigDecimal price;
}
