package com.example.movie_ticket_be.showtime.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.request.UpdateShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeDetailResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.service.AdminShowTimeService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<List<ShowTimeResponse>> createShowTimes(@RequestBody ShowTimeRequest request) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(adminShowTimeService.createShowTimes(request))
                .message("Thêm suất chiếu mới thành công")
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<ShowTimeResponse>> getAdminShowTimes(
            @Parameter(name = "filter", required = false) @Filter Specification<ShowTimes> spec,
            @ParameterObject @PageableDefault(sort = "startTime", direction = Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.<Page<ShowTimeResponse>>builder()
                .result(adminShowTimeService.getAdminShowTimes(spec, pageable))
                .message("Lấy danh sách suất chiếu thành công")
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ShowTimeDetailResponse> getAdminShowTimeDetail(@PathVariable Long id) {
        return ApiResponse.<ShowTimeDetailResponse>builder()
                .result(adminShowTimeService.getAdminShowTimeDetail(id))
                .message("Lấy chi tiết suất chiếu thành công")
                .build();
    }
}
