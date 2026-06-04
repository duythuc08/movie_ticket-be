package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.dto.request.GenreCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import com.example.movie_ticket_be.movie.service.AdminGenreService;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminGenreController {
	AdminGenreService adminGenreService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Page<GenreResponse>> getAllGenre(
			@Parameter(name = "filter", required = false) @Filter Specification<Genre> spec,
			@ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		return ApiResponse.<Page<GenreResponse>>builder().result(adminGenreService.getAllGenre(spec, pageable))
				.message("Lấy danh sách genre thành công").build();
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<GenreResponse> createGenre(@RequestBody @Valid GenreCreationRequest request) {
		return ApiResponse.<GenreResponse>builder().result(adminGenreService.createGenre(request))
				.message("Thêm genre thành công").build();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<GenreResponse> getGenreDetail(@PathVariable long id) {
		return ApiResponse.<GenreResponse>builder().result(adminGenreService.getGenreDetail(id))
				.message("Lấy thông tin chi tiết thành công").build();
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
