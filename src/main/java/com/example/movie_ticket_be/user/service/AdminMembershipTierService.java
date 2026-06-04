package com.example.movie_ticket_be.user.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.request.MembershipTierRequest;
import com.example.movie_ticket_be.user.dto.response.MembershipTierResponse;
import com.example.movie_ticket_be.user.entity.MembershipTier;
import com.example.movie_ticket_be.user.mapper.MembershipTierMapper;
import com.example.movie_ticket_be.user.repository.MembershipTierRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminMembershipTierService {

	MembershipTierRepository membershipTierRepository;
	MembershipTierMapper membershipTierMapper;

	public MembershipTierResponse createTier(MembershipTierRequest request) {
		if (membershipTierRepository.findByName(request.getName()).isPresent()) {
			throw new AppException(ErrorCode.MEMBERSHIP_TIER_EXISTED);
		}
		MembershipTier tier = MembershipTier.builder().name(request.getName()).description(request.getDescription())
				.pointsRequired(request.getPointsRequired()).discountPercent(request.getDiscountPercent())
				.birthdayDiscount(request.getBirthdayDiscount()).build();
		return membershipTierMapper.toMembershipTierResponse(membershipTierRepository.save(tier));
	}

	public List<MembershipTierResponse> getAllTiers() {
		return membershipTierRepository.findAll().stream().map(membershipTierMapper::toMembershipTierResponse).toList();
	}

	public MembershipTierResponse getTier(Long id) {
		return membershipTierMapper.toMembershipTierResponse(findById(id));
	}

	public MembershipTierResponse updateTier(Long id, MembershipTierRequest request) {
		MembershipTier tier = findById(id);
		tier.setName(request.getName());
		tier.setDescription(request.getDescription());
		tier.setPointsRequired(request.getPointsRequired());
		tier.setDiscountPercent(request.getDiscountPercent());
		tier.setBirthdayDiscount(request.getBirthdayDiscount());
		return membershipTierMapper.toMembershipTierResponse(membershipTierRepository.save(tier));
	}

	public void changeStatus(long id, EntityStatus entityStatus) {
		MembershipTier tier = findById(id);
		tier.setEntityStatus(entityStatus);
		membershipTierRepository.save(tier);
	}

	private MembershipTier findById(Long id) {
		return membershipTierRepository.findById(id)
				.orElseThrow(() -> new AppException(ErrorCode.MEMBERSHIP_TIER_NOT_FOUND));
	}
}
