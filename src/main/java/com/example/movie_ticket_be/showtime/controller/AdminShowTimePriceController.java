package com.example.movie_ticket_be.showtime.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.service.AdminShowTimePriceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/showtime-prices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminShowTimePriceController {
    AdminShowTimePriceService adminShowTimePriceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShowTimePriceResponse> createShowTimePrice(@RequestBody ShowTimePriceRequest request) {
        return ApiResponse.<ShowTimePriceResponse>builder()
                .result(adminShowTimePriceService.createShowTimePrice(request))
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ShowTimePriceResponse>> createShowTimePrices(@RequestBody List<ShowTimePriceRequest> requests) {
        return ApiResponse.<List<ShowTimePriceResponse>>builder()
                .result(adminShowTimePriceService.createShowTimePrices(requests))
                .build();
    }
}
