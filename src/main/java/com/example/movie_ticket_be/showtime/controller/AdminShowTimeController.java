package com.example.movie_ticket_be.showtime.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.request.UpdateShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.service.AdminShowTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowTimeController {
    private final AdminShowTimeService adminShowTimeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShowTimeResponse> createShowTime(@RequestBody ShowTimeRequest request) {
        return ApiResponse.<ShowTimeResponse>builder()
                .result(adminShowTimeService.createShowTime(request))
                .message("Thêm suất chiếu mới thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ShowTimeResponse>> createShowTimes(@RequestBody List<ShowTimeRequest> requests) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(adminShowTimeService.createShowTimes(requests))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShowTimeResponse> updateShowTime(@PathVariable Long id, @RequestBody UpdateShowTimeRequest request) {
        return ApiResponse.<ShowTimeResponse>builder()
                .result(adminShowTimeService.updateShowTime(id, request))
                .message("Cập nhật suất chiếu thành công")
                .build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShowTimeResponse> cancelShowTime(@PathVariable Long id) {
        return ApiResponse.<ShowTimeResponse>builder()
                .result(adminShowTimeService.cancelShowTime(id))
                .message("Hủy suất chiếu thành công")
                .build();
    }
}
