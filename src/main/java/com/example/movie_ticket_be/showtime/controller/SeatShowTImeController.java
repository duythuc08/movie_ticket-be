package com.example.movie_ticket_be.showtime.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.response.SeatShowTimeResponse;
import com.example.movie_ticket_be.showtime.service.SeatShowTimeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/seatShowTimes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class SeatShowTImeController {
    SeatShowTimeService seatShowTimeService;

    @GetMapping("/getSeatShowTimes/by-showTime/{showTimeId}")
    public ApiResponse<List<SeatShowTimeResponse>> getAllSeatShowTimesByShowTime(@PathVariable Long showTimeId){
        return ApiResponse.<List<SeatShowTimeResponse>>builder()
                .result(seatShowTimeService.getAllSeatShowTimesByShowTime(showTimeId))
                .build();
    }
}
