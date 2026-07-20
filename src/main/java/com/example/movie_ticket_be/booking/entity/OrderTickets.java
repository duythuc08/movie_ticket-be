package com.example.movie_ticket_be.booking.entity;

import com.example.movie_ticket_be.booking.enums.TicketStatus;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderTickets extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	Long orderTicketId;

	@Column(nullable = false)
	BigDecimal price;

	@Enumerated(EnumType.STRING)
	TicketStatus ticketStatus;

	@ManyToOne
	@JoinColumn(name = "order_id")
	Orders orders;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_show_time_id")
	SeatShowTime seatShowTime;

	String snapshotSeatRow;
	Integer snapshotSeatNumber;

	@Enumerated(EnumType.STRING)
	SeatType snapshotSeatType;

	LocalDateTime snapshotShowTimeStart;
	String snapshotMovieTitle;
	String snapshotRoomName;
	String snapshotCinemaName;
}
