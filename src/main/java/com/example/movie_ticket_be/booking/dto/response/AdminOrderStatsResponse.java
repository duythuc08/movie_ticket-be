package com.example.movie_ticket_be.booking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOrderStatsResponse {
	BigDecimal totalRevenue;
	Long totalOrders;
	Long paidOrders;
	Long cancelledOrders;
	Long pendingOrders;
	Long expiredOrders;
	Long usedOrders;
}
