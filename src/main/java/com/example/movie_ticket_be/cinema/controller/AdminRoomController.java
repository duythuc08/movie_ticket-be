package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.request.AdminRoomRequest;
import com.example.movie_ticket_be.cinema.dto.request.AdminSeatUpdate;
import com.example.movie_ticket_be.cinema.dto.request.RoomRequest;
import com.example.movie_ticket_be.cinema.dto.request.SeatSetupRequest;
import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.dto.response.SeatResponse;
import com.example.movie_ticket_be.cinema.service.AdminRoomService;
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
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminRoomController {
    AdminRoomService adminRoomService;
    AdminSeatService adminSeatService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoomResponse> createRoom(@RequestBody RoomRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .result(adminRoomService.createRoom(request))
                .message("Thêm phòng chiếu thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoomResponse>> createRooms(@RequestBody List<RoomRequest> requests) {
        return ApiResponse.<List<RoomResponse>>builder()
                .result(adminRoomService.createRooms(requests))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoomResponse> updateRoom(@PathVariable long id, @RequestBody AdminRoomRequest request) {
        return ApiResponse.<RoomResponse>builder()
                .result(adminRoomService.updateRoom(id, request))
                .message("Cập nhật phòng chiếu thành công")
                .build();
    }

    @PostMapping("/{id}/seats/setup")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<SeatResponse>> setupSeats(@PathVariable long id, @RequestBody SeatSetupRequest request) {
        return ApiResponse.<List<SeatResponse>>builder()
                .result(adminSeatService.setUpSeatsForRoom(id, request))
                .message("Thiết lập ghế phòng chiếu thành công")
                .build();
    }

    @PutMapping("/{roomId}/seats/{seatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SeatResponse> updateSeatType(@PathVariable long roomId, @PathVariable long seatId,
            @RequestBody AdminSeatUpdate request) {
        return ApiResponse.<SeatResponse>builder()
                .result(adminSeatService.updateSeatType(seatId, request))
                .message("Cập nhật loại ghế thành công")
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminRoomService.changeStatus(id, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt phòng chiếu thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminRoomService.changeStatus(id, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa phòng chiếu thành công").build();
    }
}
