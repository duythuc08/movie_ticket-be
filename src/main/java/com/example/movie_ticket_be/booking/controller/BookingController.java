package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.request.BookingRequest;
import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.service.BookingService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class BookingController {
    BookingService bookingService;

    @PostMapping("/create")
    public ApiResponse<OrderResponse> createBooking(@RequestBody BookingRequest bookingRequest){
        OrderResponse orderResponse = bookingService.createBooking(bookingRequest);

        return ApiResponse.<OrderResponse>builder()
                .message("Tạo đơn hàng thành công")
                .result(orderResponse)
                .build();
    }
}
