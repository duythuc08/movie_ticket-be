package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.request.RoomRequest;
import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.service.AdminRoomService;
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
