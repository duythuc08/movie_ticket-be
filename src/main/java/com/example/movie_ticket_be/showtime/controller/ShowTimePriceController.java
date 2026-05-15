package com.example.movie_ticket_be.showtime.controller;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.service.ShowTimePriceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/showTimePrice")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ShowTimePriceController {
    ShowTimePriceService service;

    @GetMapping("/getPrice/by-showtime/{showTimeId}")
    ApiResponse<List<ShowTimePriceResponse>> getAllPriceByShowTime(@PathVariable Long showTimeId){
        return ApiResponse.<List<ShowTimePriceResponse>>builder()
                .result(service.getAllPriceByShowTime(showTimeId))
                .build();
    }

    @GetMapping("/getPrice/by-showtime/{showTimeId}/type")
    ApiResponse<ShowTimePriceResponse> getPriceByShowTimeAndSeatType (@PathVariable Long showTimeId, @RequestParam SeatType seatType){
        return ApiResponse.<ShowTimePriceResponse>builder()
                .result(service.getPriceByShowTimeAndSeatType(showTimeId, seatType))
                .build();
    }
}
