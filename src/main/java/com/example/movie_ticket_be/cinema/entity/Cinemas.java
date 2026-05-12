package com.example.movie_ticket_be.cinema.entity;

import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "cinema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cinemas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long cinemaId;

    String name;
    String address;
    @Column(unique = true)
    String phoneNumber;
    @Column(unique = true)
    String email;

    @Enumerated(EnumType.STRING)
    CinemaStatus cinemaStatus;
}
