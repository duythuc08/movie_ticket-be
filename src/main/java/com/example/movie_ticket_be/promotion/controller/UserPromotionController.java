package com.example.movie_ticket_be.promotion.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.promotion.dto.response.PublicPromotionResponse;
import com.example.movie_ticket_be.promotion.dto.response.UserVoucherResponse;
import com.example.movie_ticket_be.promotion.service.UserPromotionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPromotionController {

	UserPromotionService userPromotionService;

	@GetMapping("/promotions")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<PublicPromotionResponse>> getAvailablePromotions() {
		return ApiResponse.<List<PublicPromotionResponse>>builder()
				.result(userPromotionService.getAvailablePromotions()).build();
	}

	@PostMapping("/promotions/{promotionId}/claim")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> claimPromotion(@PathVariable Long promotionId) {
		userPromotionService.claimPromotion(promotionId);
		return ApiResponse.<Void>builder().message("Nhận voucher thành công").build();
	}

	@PostMapping("/promotions/claim-by-code")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<Void> claimPromotionByCode(@RequestParam String code) {
		userPromotionService.claimPromotionByCode(code);
		return ApiResponse.<Void>builder().message("Nhận voucher thành công").build();
	}

	@GetMapping("/users/vouchers")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<UserVoucherResponse>> getMyVouchers() {
		return ApiResponse.<List<UserVoucherResponse>>builder().result(userPromotionService.getMyVouchers()).build();
	}

	@GetMapping("/users/vouchers/applicable")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<List<UserVoucherResponse>> getApplicableVouchers(@RequestParam(required = false) Long movieId,
			@RequestParam(required = false) BigDecimal totalAmount) {
		return ApiResponse.<List<UserVoucherResponse>>builder()
				.result(userPromotionService.getApplicableVouchers(movieId, totalAmount)).build();
	}
}
