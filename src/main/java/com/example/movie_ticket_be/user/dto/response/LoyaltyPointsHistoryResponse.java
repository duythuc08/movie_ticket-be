package com.example.movie_ticket_be.user.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoyaltyPointsHistoryResponse {
	Long historyId;
	int pointsChange;
	String description;
	int oldBalance;
	int newBalance;
	LocalDateTime createdAt;
	Long orderId;
}
