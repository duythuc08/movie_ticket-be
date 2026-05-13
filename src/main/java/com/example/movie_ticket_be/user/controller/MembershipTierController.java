package com.example.movie_ticket_be.user.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.user.dto.response.MembershipTierResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/membership-tiers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MembershipTierController {
    com.example.movie_ticket_be.user.service.MembershipTierService membershipTierService;

    @GetMapping("/getMembershipTier/{name}")
    public ApiResponse<MembershipTierResponse> getMembershipTier(@PathVariable String name) {
        return ApiResponse.<MembershipTierResponse>builder()
                .result(membershipTierService.getMembershipTierByName(name))
                .build();
    }
    @GetMapping("/getAllMembershipTiers")
    public ApiResponse<List<MembershipTierResponse>> getMembershipTiers() {
        return ApiResponse.<List<MembershipTierResponse>>builder()
                .result(membershipTierService.getMembershipTierList())
                .build();
    }
}
