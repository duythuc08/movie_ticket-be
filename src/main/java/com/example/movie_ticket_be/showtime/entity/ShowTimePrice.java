package com.example.movie_ticket_be.showtime.entity;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimePrice extends BaseEntity {
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
