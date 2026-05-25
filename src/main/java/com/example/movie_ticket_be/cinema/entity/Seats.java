package com.example.movie_ticket_be.cinema.entity;

import java.math.BigDecimal;

import com.example.movie_ticket_be.cinema.enums.SeatStatus;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seats extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long seatId;
    String seatRow;
    Integer seatNumber;
    
    @Column(precision = 4, scale = 2)
    BigDecimal viewQuanlityScore;
    @ManyToOne
    @JoinColumn(name = "room_id")
    Rooms rooms;

    @Enumerated(EnumType.STRING)
    SeatType seatType;

    @Enumerated(EnumType.STRING)
    SeatStatus seatStatus;
}
