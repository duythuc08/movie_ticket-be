package com.example.movie_ticket_be.cinema.entity;

import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "cinema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cinemas extends BaseEntity {
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
