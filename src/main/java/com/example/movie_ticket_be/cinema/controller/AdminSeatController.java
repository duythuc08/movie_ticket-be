package com.example.movie_ticket_be.cinema.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.cinema.dto.request.AdminSeatStatusUpdateRequest;
import com.example.movie_ticket_be.cinema.dto.response.SeatResponse;
import com.example.movie_ticket_be.cinema.service.AdminSeatService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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

    @GetMapping("/{seatId}/blocked-showtimes")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ShowTimeResponse>> getBlockedShowTimesForSeat(@PathVariable Long seatId) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(adminSeatService.getBlockedUpcomingShowTimesBySeat(seatId))
                .message("Lấy danh sách suất chiếu bị khóa thành công")
                .build();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SeatResponse> updateSeatStatus(
            @PathVariable long id,
            @RequestBody AdminSeatStatusUpdateRequest request) {
        return ApiResponse.<SeatResponse>builder()
                .result(adminSeatService.updateSeatStatus(id, request))
                .message("Cập nhật trạng thái ghế thành công")
                .build();
    }
    
}
