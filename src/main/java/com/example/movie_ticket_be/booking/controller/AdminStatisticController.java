package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.response.StatisticResponse;
import com.example.movie_ticket_be.booking.service.AdminStatisticService;
import com.example.movie_ticket_be.core.dto.ApiResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminStatisticController {

    AdminStatisticService adminStatisticService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<StatisticResponse> getMonthlyStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        LocalDate today = LocalDate.now();
        if (year == null) year = today.getYear();
        if (month == null) month = today.getMonthValue();
        
        StatisticResponse result = adminStatisticService.getMonthlyStatistics(year, month);
        return ApiResponse.<StatisticResponse>builder().result(result).build();
    }
}
