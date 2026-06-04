package com.example.movie_ticket_be.promotion.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.promotion.dto.response.PublicPromotionResponse;
import com.example.movie_ticket_be.promotion.dto.response.UserVoucherResponse;
import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.entity.UserPromotion;
import com.example.movie_ticket_be.promotion.enums.PromotionStatus;
import com.example.movie_ticket_be.promotion.repository.PromotionRepository;
import com.example.movie_ticket_be.promotion.repository.UserPromotionRepository;
import com.example.movie_ticket_be.showtime.enums.DayOfWeek;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPromotionService {

	PromotionRepository promotionRepository;
	UserPromotionRepository userPromotionRepository;
	UserRepository userRepository;

	public List<PublicPromotionResponse> getAvailablePromotions() {
		String userId = getCurrentUserId();
		LocalDateTime now = LocalDateTime.now();
		return promotionRepository.findByStatus(PromotionStatus.PUBLISHED).stream()
				.filter(p -> !now.isAfter(p.getEndTime()) && !now.isBefore(p.getStartTime()))
				.filter(p -> p.getUseLimit() == null || p.getUsedCount() < p.getUseLimit())
				.filter(p -> !userPromotionRepository.existsByUsers_UserIdAndPromotion_PromotionId(userId,
						p.getPromotionId()))
				.map(this::toPublicResponse).collect(Collectors.toList());
	}

	public void claimPromotion(Long promotionId) {
		String userId = getCurrentUserId();
		Promotion promotion = promotionRepository.findByPromotionId(promotionId)
				.orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

		if (promotion.getStatus() != PromotionStatus.PUBLISHED) {
			throw new AppException(ErrorCode.PROMOTION_NOT_PUBLISHED);
		}
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(promotion.getEndTime()) || now.isBefore(promotion.getStartTime())) {
			throw new AppException(ErrorCode.PROMOTION_EXPIRED);
		}
		if (promotion.getUseLimit() != null && promotion.getUsedCount() >= promotion.getUseLimit()) {
			throw new AppException(ErrorCode.PROMOTION_OUT_OF_STOCK);
		}
		if (userPromotionRepository.existsByUsers_UserIdAndPromotion_PromotionId(userId, promotionId)) {
			throw new AppException(ErrorCode.PROMOTION_ALREADY_CLAIMED);
		}

		Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		userPromotionRepository.save(UserPromotion.builder().users(user).promotion(promotion).isUsed(false).build());
	}

	public List<UserVoucherResponse> getMyVouchers() {
		String userId = getCurrentUserId();
		return userPromotionRepository.findByUsers_UserIdAndIsUsedFalse(userId).stream()
				.map(up -> toVoucherResponse(up, null)).collect(Collectors.toList());
	}

	public List<UserVoucherResponse> getApplicableVouchers(Long movieId, BigDecimal totalAmount) {
		String userId = getCurrentUserId();
		LocalDateTime now = LocalDateTime.now();
		DayOfWeek today = DayOfWeek.values()[now.getDayOfWeek().ordinal()];

		return userPromotionRepository.findByUsers_UserIdAndIsUsedFalse(userId).stream()
				.map(up -> toVoucherResponse(up, checkEligible(up.getPromotion(), movieId, totalAmount, now, today)))
				.collect(Collectors.toList());
	}

	private boolean checkEligible(Promotion p, Long movieId, BigDecimal totalAmount, LocalDateTime now,
			DayOfWeek today) {
		if (p.getStatus() != PromotionStatus.PUBLISHED)
			return false;
		if (now.isAfter(p.getEndTime()) || now.isBefore(p.getStartTime()))
			return false;
		if (p.getUseLimit() != null && p.getUsedCount() >= p.getUseLimit())
			return false;
		if (p.getMinOrderValue() != null && totalAmount != null && totalAmount.compareTo(p.getMinOrderValue()) < 0)
			return false;
		if (p.getApplicableMovies() != null && !p.getApplicableMovies().isEmpty() && movieId != null) {
			boolean match = p.getApplicableMovies().stream().anyMatch(m -> m.getMovieId().equals(movieId));
			if (!match)
				return false;
		}
		if (p.getDayOfWeek() != null && !p.getDayOfWeek().isEmpty()) {
			if (!p.getDayOfWeek().contains(today))
				return false;
		}
		return true;
	}

	private PublicPromotionResponse toPublicResponse(Promotion p) {
		return PublicPromotionResponse.builder().promotionId(p.getPromotionId()).code(p.getCode()).name(p.getName())
				.description(p.getDescription()).type(p.getType()).discountValue(p.getDiscountValue())
				.minOrderValue(p.getMinOrderValue()).maxDiscountAmount(p.getMaxDiscountAmount())
				.useLimit(p.getUseLimit()).usedCount(p.getUsedCount()).startTime(p.getStartTime())
				.endTime(p.getEndTime()).dayOfWeek(p.getDayOfWeek())
				.applicableMovieIds(toMovieIds(p.getApplicableMovies())).build();
	}

	private UserVoucherResponse toVoucherResponse(UserPromotion up, Boolean eligible) {
		Promotion p = up.getPromotion();
		return UserVoucherResponse.builder().voucherId(up.getId()).promotionId(p.getPromotionId()).code(p.getCode())
				.name(p.getName()).description(p.getDescription()).type(p.getType()).discountValue(p.getDiscountValue())
				.minOrderValue(p.getMinOrderValue()).maxDiscountAmount(p.getMaxDiscountAmount())
				.startTime(p.getStartTime()).endTime(p.getEndTime()).dayOfWeek(p.getDayOfWeek())
				.applicableMovieIds(toMovieIds(p.getApplicableMovies())).claimedAt(up.getClaimedAt()).eligible(eligible)
				.build();
	}

	private Set<Long> toMovieIds(Set<Movies> movies) {
		if (movies == null)
			return null;
		return movies.stream().map(Movies::getMovieId).collect(Collectors.toSet());
	}

	private String getCurrentUserId() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
				.getUserId();
	}
}
