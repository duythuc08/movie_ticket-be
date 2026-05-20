package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.dto.request.GenreCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.service.AdminGenreService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminGenreController {
    AdminGenreService adminGenreService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GenreResponse> createGenre(@RequestBody @Valid GenreCreationRequest request) {
        return ApiResponse.<GenreResponse>builder()
                .result(adminGenreService.createGenre(request))
                .message("Thêm genre thành công")
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminGenreService.changeStatus(id, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt genre thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminGenreService.changeStatus(id, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa genre thành công").build();
    }
}
