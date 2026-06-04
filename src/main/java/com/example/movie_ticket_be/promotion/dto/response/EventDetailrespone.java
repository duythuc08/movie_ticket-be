package com.example.movie_ticket_be.promotion.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDetailrespone {
	String title;
	String description;
	String posterUrl;
	LocalDateTime startTime;
	LocalDateTime endTime;
	String movieTitle;
}
