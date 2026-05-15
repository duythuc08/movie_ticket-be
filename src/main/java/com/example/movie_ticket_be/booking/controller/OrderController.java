package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.service.OrderService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(orderId))
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderResponse>> getOrderByUserId(@PathVariable String userId) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getOrdersByUserId(userId))
                .build();
    }
}
