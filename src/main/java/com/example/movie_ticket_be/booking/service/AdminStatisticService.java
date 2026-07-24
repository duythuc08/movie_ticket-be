package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.response.DailyRevenueResponse;
import com.example.movie_ticket_be.booking.dto.response.StatisticResponse;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatisticService {

    OrderRepository orderRepository;

    public StatisticResponse getMonthlyStatistics(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime from = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime to = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);

        List<Object[]> rawData = orderRepository.sumRevenueAndOrdersPerDay(OrderStatus.PAID, from, to);

        // Convert raw data to Map<Day, DailyRevenueResponse>
        Map<Integer, DailyRevenueResponse> dataMap = rawData.stream().collect(Collectors.toMap(
                obj -> ((Number) obj[0]).intValue(),
                obj -> DailyRevenueResponse.builder()
                        .day(String.valueOf(((Number) obj[0]).intValue()))
                        .revenue((BigDecimal) obj[1])
                        .ordersCount(((Number) obj[2]).longValue())
                        .build()
        ));

        List<DailyRevenueResponse> dailyData = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;

        int daysInMonth = yearMonth.lengthOfMonth();
        
        // Ensure not to show future days if current month
        LocalDate today = LocalDate.now();
        int endDay = (year == today.getYear() && month == today.getMonthValue()) ? today.getDayOfMonth() : daysInMonth;

        for (int i = 1; i <= endDay; i++) {
            DailyRevenueResponse dayData = dataMap.getOrDefault(i, DailyRevenueResponse.builder()
                    .day(String.valueOf(i))
                    .revenue(BigDecimal.ZERO)
                    .ordersCount(0L)
                    .build());
            
            dailyData.add(dayData);
            if (dayData.getRevenue() != null) {
                totalRevenue = totalRevenue.add(dayData.getRevenue());
            }
            if (dayData.getOrdersCount() != null) {
                totalOrders += dayData.getOrdersCount();
            }
        }

        return StatisticResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .dailyData(dailyData)
                .build();
    }
}
