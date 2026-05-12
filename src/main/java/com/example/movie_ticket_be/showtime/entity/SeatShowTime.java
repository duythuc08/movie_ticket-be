package com.example.movie_ticket_be.showtime.entity;

import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.cinema.entity.Seats;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.user.entity.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SeatShowTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long seatShowTimeId;
    LocalDateTime lockedUntil;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Users users;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    Seats seats;

    @ManyToOne
    @JoinColumn(name = "show_time_id")
    ShowTimes showTimes;

    @OneToMany(mappedBy = "seatShowTime", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderTickets> orderTickets;

    @Enumerated(EnumType.STRING)
    SeatShowTimeStatus seatShowTimeStatus;
}
