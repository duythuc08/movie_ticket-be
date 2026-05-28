package com.example.movie_ticket_be.showtime.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.response.SeatSelectionResponse;
import com.example.movie_ticket_be.showtime.service.SeatShowTimeService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/seatShowTimes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatShowTImeController {
    SeatShowTimeService seatShowTimeService;

    @GetMapping("/selection/{showTimeId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SeatSelectionResponse> getSeatSelection(@PathVariable Long showTimeId) {
        return ApiResponse.<SeatSelectionResponse>builder()
                .result(seatShowTimeService.getSeatSelectionData(showTimeId))
                .build();
    }
}
