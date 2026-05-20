package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.request.CinemaRequest;
import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import com.example.movie_ticket_be.cinema.service.AdminCinemaService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/cinemas")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCinemaController {
    AdminCinemaService adminCinemaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CinemaResponse> createCinema(@RequestBody CinemaRequest request) {
        return ApiResponse.<CinemaResponse>builder()
                .result(adminCinemaService.createCinema(request))
                .message("Thêm rạp chiếu thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CinemaResponse>> createCinemas(@RequestBody List<CinemaRequest> requests) {
        return ApiResponse.<List<CinemaResponse>>builder()
                .result(adminCinemaService.createCinemas(requests))
                .build();
    }

    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CinemaResponse>> getCinemasByStatus(@RequestParam CinemaStatus status) {
        return ApiResponse.<List<CinemaResponse>>builder()
                .result(adminCinemaService.getCinemasByStatus(status))
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminCinemaService.changeStatus(id, CinemaStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt rạp chiếu thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminCinemaService.changeStatus(id, CinemaStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa rạp chiếu thành công").build();
    }
}
