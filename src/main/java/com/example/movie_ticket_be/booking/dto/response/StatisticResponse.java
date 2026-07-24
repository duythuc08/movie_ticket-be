package com.example.movie_ticket_be.booking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticResponse {
    BigDecimal totalRevenue;
    Long totalOrders;
    List<DailyRevenueResponse> dailyData;
}
