package com.example.movie_ticket_be.user.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.recommendation.dto.response.AdminUserRecommendationResponse;
import com.example.movie_ticket_be.recommendation.service.AdminRecommendationService;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.request.UsersCreationRequest;
import com.example.movie_ticket_be.user.dto.response.LoyaltyPointsHistoryResponse;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.service.AdminLoyaltyPointsHistoryService;
import com.example.movie_ticket_be.user.service.AdminUserService;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {
	AdminUserService adminUserService;
	AdminLoyaltyPointsHistoryService adminLoyaltyPointsHistoryService;
	AdminRecommendationService adminRecommendationService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UsersRespone> createUser(@RequestBody @Valid UsersCreationRequest request) {
		return ApiResponse.<UsersRespone>builder().result(adminUserService.createUser(request)).build();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Page<UsersRespone>> getUsers(
			@Parameter(name = "filter", required = false) @Filter Specification<Users> spec,
			@ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		return ApiResponse.<Page<UsersRespone>>builder().result(adminUserService.getUsers(spec, pageable)).build();
	}

	@GetMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UsersRespone> getUser(@PathVariable String userId) {
		return ApiResponse.<UsersRespone>builder().result(adminUserService.getUserById(userId)).build();
	}

	@GetMapping("/{userId}/loyalty-history")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Page<LoyaltyPointsHistoryResponse>> getLoyaltyHistory(@PathVariable String userId,
			@ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		return ApiResponse.<Page<LoyaltyPointsHistoryResponse>>builder()
				.result(adminLoyaltyPointsHistoryService.getHistoriesByUserId(userId, pageable)).build();
	}

	@PutMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UsersRespone> updateUser(@PathVariable String userId,
			@RequestBody @Valid UserUpdateRequest request) {
		return ApiResponse.<UsersRespone>builder().result(adminUserService.updateUser(userId, request)).build();
	}

	@DeleteMapping("/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> deleteUser(@PathVariable String userId) {
		adminUserService.deleteUser(userId);
		return ApiResponse.<String>builder().result("User has been deleted").build();
	}

	@PutMapping("/{userId}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> activate(@PathVariable String userId) {
		adminUserService.changeStatus(userId, EntityStatus.ACTIVE);
		return ApiResponse.<Void>builder().message("Kích hoạt người dùng thành công").build();
	}

	@PutMapping("/{userId}/inactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> inactivate(@PathVariable String userId) {
		adminUserService.changeStatus(userId, EntityStatus.INACTIVE);
		return ApiResponse.<Void>builder().message("Vô hiệu hóa người dùng thành công").build();
	}

	@PutMapping("/{userId}/banned")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> banUser(@PathVariable String userId) {
		adminUserService.banUser(userId);
		return ApiResponse.<Void>builder().message("Người dùng đã bị cấm").build();
	}

	@GetMapping("/{userId}/recommendations")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminUserRecommendationResponse> getUserRecommendations(@PathVariable String userId) {
		return ApiResponse.<AdminUserRecommendationResponse>builder()
				.result(adminRecommendationService.getRecommendationsForAdmin(userId))
				.build();
	}
}
