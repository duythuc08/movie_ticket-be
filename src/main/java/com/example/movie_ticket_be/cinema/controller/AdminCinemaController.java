package com.example.movie_ticket_be.cinema.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.cinema.dto.request.AdminCinemaUpdateRequest;
import com.example.movie_ticket_be.cinema.dto.request.CinemaRequest;
import com.example.movie_ticket_be.cinema.dto.response.AdminCinemaResponse;
import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import com.example.movie_ticket_be.cinema.service.AdminCinemaService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminCinemaResponse> getAdminCinemaById(@PathVariable long id) {
        return ApiResponse.<AdminCinemaResponse>builder()
                .result(adminCinemaService.getAdminCinemaById(id))
                .message("Lấy thông tin rạp chiếu thành công")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<CinemaResponse>> getAllCinemas(
        @Parameter(name = "filter", required = false) @Filter Specification<Cinemas> spec,
        @ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.<Page<CinemaResponse>>builder()
                .result(adminCinemaService.getCinemas(spec, pageable))
                .build();
    }

    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CinemaResponse>> getCinemasByStatus(@RequestParam CinemaStatus status) {
        return ApiResponse.<List<CinemaResponse>>builder()
                .result(adminCinemaService.getCinemasByStatus(status))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminCinemaResponse> updateCinema(@PathVariable long id, @RequestBody AdminCinemaUpdateRequest request) {
        return ApiResponse.<AdminCinemaResponse>builder()
                .result(adminCinemaService.updateCinema(id, request))
                .message("Cập nhật rạp chiếu thành công")
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
