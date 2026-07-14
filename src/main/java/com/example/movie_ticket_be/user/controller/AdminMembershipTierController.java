package com.example.movie_ticket_be.user.controller;

import jakarta.validation.Valid;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.user.dto.request.MembershipTierRequest;
import com.example.movie_ticket_be.user.dto.response.MembershipTierResponse;
import com.example.movie_ticket_be.user.service.AdminMembershipTierService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/membership-tiers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminMembershipTierController {

	AdminMembershipTierService adminMembershipTierService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<MembershipTierResponse> createTier(@Valid @RequestBody MembershipTierRequest request) {
		return ApiResponse.<MembershipTierResponse>builder().result(adminMembershipTierService.createTier(request))
				.message("Tạo hạng thành viên thành công").build();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<List<MembershipTierResponse>> getAllTiers() {
		return ApiResponse.<List<MembershipTierResponse>>builder().result(adminMembershipTierService.getAllTiers())
				.build();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<MembershipTierResponse> getTier(@PathVariable Long id) {
		return ApiResponse.<MembershipTierResponse>builder().result(adminMembershipTierService.getTier(id)).build();
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<MembershipTierResponse> updateTier(@PathVariable Long id,
			@Valid @RequestBody MembershipTierRequest request) {
		return ApiResponse.<MembershipTierResponse>builder().result(adminMembershipTierService.updateTier(id, request))
				.message("Cập nhật hạng thành viên thành công").build();
	}

	@PutMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> activate(@PathVariable long id) {
		adminMembershipTierService.changeStatus(id, EntityStatus.ACTIVE);
		return ApiResponse.<Void>builder().message("Kích hoạt hạng thành viên thành công").build();
	}

	@PutMapping("/{id}/inactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> inactivate(@PathVariable long id) {
		adminMembershipTierService.changeStatus(id, EntityStatus.INACTIVE);
		return ApiResponse.<Void>builder().message("Vô hiệu hóa hạng thành viên thành công").build();
	}
}
