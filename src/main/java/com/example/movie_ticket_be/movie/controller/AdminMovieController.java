package com.example.movie_ticket_be.movie.controller;

import java.util.List;

import com.turkraft.springfilter.boot.Filter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.dto.request.MovieCreationRequest;
import com.example.movie_ticket_be.movie.dto.request.MovieUpdateRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminMovieResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.service.AdminMovieService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/movies")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AdminMovieController {
    AdminMovieService adminMovieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<MovieResponse> createAdminMovie(@RequestBody @Valid MovieCreationRequest request) {
        return ApiResponse.<MovieResponse>builder()
                .result(adminMovieService.createAdminMovie(request))
                .message("Thêm phim mới thành công")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<AdminMovieResponse>> getAdminMovies(
            @Parameter(name = "filter", required = false) @Filter Specification<Movies> spec,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable

    ) {
        return ApiResponse.<Page<AdminMovieResponse>>builder()
                .result(adminMovieService.getAdminMovies(spec, pageable))
                .message("Lấy danh sách phim thành công")
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminMovieResponse> getAdminMovieDetail(@PathVariable long id){
        return ApiResponse.<AdminMovieResponse>builder()
                .result(adminMovieService.getAdminMovieDetail(id))
                .message("Lấy thông tin phim thành công")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminMovieResponse> updateAdminMovie(
            @PathVariable long id,
            @RequestBody @Valid MovieUpdateRequest request
    ) {
        return ApiResponse.<AdminMovieResponse>builder()
                .result(adminMovieService.updateAdminMovie(id, request))
                .message("Cập nhật phim thành công")
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminMovieService.changeStatus(id, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt phim thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminMovieService.changeStatus(id, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa phim thành công").build();
    }
}
