package com.example.movie_ticket_be.promotion.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.promotion.dto.request.PromotionRequest;
import com.example.movie_ticket_be.promotion.dto.response.AdminPromotionResponse;
import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.service.AdminPromotionService;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPromotionController {

	AdminPromotionService adminPromotionService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminPromotionResponse> createPromotion(@RequestBody PromotionRequest request) {
		return ApiResponse.<AdminPromotionResponse>builder().result(adminPromotionService.createPromotion(request))
				.message("Tạo khuyến mãi thành công").build();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Page<AdminPromotionResponse>> getAllPromotions(
			@Parameter(name = "filter", required = false) @Filter Specification<Promotion> spec,
			@ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		return ApiResponse.<Page<AdminPromotionResponse>>builder()
				.result(adminPromotionService.getAllPromotions(spec, pageable)).build();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminPromotionResponse> getPromotion(@PathVariable Long id) {
		return ApiResponse.<AdminPromotionResponse>builder().result(adminPromotionService.getPromotion(id)).build();
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminPromotionResponse> updatePromotion(@PathVariable Long id,
			@RequestBody PromotionRequest request) {
		return ApiResponse.<AdminPromotionResponse>builder().result(adminPromotionService.updatePromotion(id, request))
				.message("Cập nhật khuyến mãi thành công").build();
	}

	@PutMapping("/{id}/submit")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> submit(@PathVariable Long id) {
		adminPromotionService.submit(id);
		return ApiResponse.<Void>builder().message("Gửi duyệt thành công").build();
	}

	@PutMapping("/{id}/approve")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> approve(@PathVariable Long id) {
		adminPromotionService.approve(id);
		return ApiResponse.<Void>builder().message("Phê duyệt và đăng khuyến mãi thành công").build();
	}

	@PutMapping("/{id}/pause")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> pause(@PathVariable Long id) {
		adminPromotionService.pause(id);
		return ApiResponse.<Void>builder().message("Tạm dừng khuyến mãi thành công").build();
	}

	@PutMapping("/{id}/resume")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> resume(@PathVariable Long id) {
		adminPromotionService.resume(id);
		return ApiResponse.<Void>builder().message("Kích hoạt lại khuyến mãi thành công").build();
	}
}
