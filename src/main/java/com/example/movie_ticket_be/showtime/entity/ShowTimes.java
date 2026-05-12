package com.example.movie_ticket_be.showtime.entity;

import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "show_time")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShowTimes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long showTimeId;

    LocalDateTime startTime;
    LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    Movies movies;

    @ManyToOne
    @JoinColumn(name = "room_id")
    Rooms rooms;

    @OneToMany(mappedBy = "showtimes", cascade = CascadeType.ALL)
    Set<ShowTimePrice> prices;

    @Enumerated(EnumType.STRING)
    ShowTimeStatus showTimeStatus;
}
