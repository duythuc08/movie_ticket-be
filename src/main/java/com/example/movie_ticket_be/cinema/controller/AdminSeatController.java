package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.response.SeatResponse;
import com.example.movie_ticket_be.cinema.service.AdminSeatService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/seats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminSeatController {
    AdminSeatService adminSeatService;

    @GetMapping("/by-room/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SeatResponse>> getSeatsByRoom(@PathVariable Long roomId) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(adminSeatService.getSeatsByRoom(roomId))
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminSeatService.changeStatus(id, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt ghế thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminSeatService.changeStatus(id, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa ghế thành công").build();
    }
}
