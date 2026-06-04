package com.example.movie_ticket_be.promotion.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.promotion.dto.request.PromotionRequest;
import com.example.movie_ticket_be.promotion.dto.response.AdminPromotionResponse;
import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.enums.PromotionStatus;
import com.example.movie_ticket_be.promotion.mapper.PromotionMapper;
import com.example.movie_ticket_be.promotion.repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPromotionService {

	PromotionRepository promotionRepository;
	PromotionMapper promotionMapper;
	MovieRepository movieRepository;

	public AdminPromotionResponse createPromotion(PromotionRequest request) {
		if (promotionRepository.existsByCode(request.getCode())) {
			throw new AppException(ErrorCode.PROMOTION_EXISTED);
		}
		Promotion promotion = promotionMapper.toPromotion(request);
		promotion.setStatus(PromotionStatus.DRAFT);
		promotion.setUsedCount(0);
		promotion.setApplicableMovies(resolveMovies(request.getApplicableMovieIds()));
		return promotionMapper.toAdminResponse(promotionRepository.save(promotion));
	}

	public Page<AdminPromotionResponse> getAllPromotions(Specification<Promotion> spec, Pageable pageable) {
		return promotionRepository.findAll(spec, pageable).map(promotionMapper::toAdminResponse);
	}

	public AdminPromotionResponse getPromotion(Long id) {
		return promotionMapper.toAdminResponse(findById(id));
	}

	public AdminPromotionResponse updatePromotion(Long id, PromotionRequest request) {
		Promotion promotion = findById(id);
		if (promotion.getStatus() != PromotionStatus.DRAFT) {
			throw new AppException(ErrorCode.PROMOTION_STATUS_INVALID);
		}
		promotionMapper.updatePromotion(promotion, request);
		promotion.setApplicableMovies(resolveMovies(request.getApplicableMovieIds()));
		return promotionMapper.toAdminResponse(promotionRepository.save(promotion));
	}

	public void submit(Long id) {
		Promotion promotion = findById(id);
		if (promotion.getStatus() != PromotionStatus.DRAFT) {
			throw new AppException(ErrorCode.PROMOTION_STATUS_INVALID);
		}
		promotion.setStatus(PromotionStatus.PENDING_APPROVAL);
		promotionRepository.save(promotion);
	}

	public void approve(Long id) {
		Promotion promotion = findById(id);
		if (promotion.getStatus() != PromotionStatus.PENDING_APPROVAL) {
			throw new AppException(ErrorCode.PROMOTION_STATUS_INVALID);
		}
		promotion.setStatus(PromotionStatus.PUBLISHED);
		promotionRepository.save(promotion);
	}

	public void pause(Long id) {
		Promotion promotion = findById(id);
		if (promotion.getStatus() != PromotionStatus.PUBLISHED) {
			throw new AppException(ErrorCode.PROMOTION_STATUS_INVALID);
		}
		promotion.setStatus(PromotionStatus.PAUSED);
		promotionRepository.save(promotion);
	}

	public void resume(Long id) {
		Promotion promotion = findById(id);
		if (promotion.getStatus() != PromotionStatus.PAUSED) {
			throw new AppException(ErrorCode.PROMOTION_STATUS_INVALID);
		}
		promotion.setStatus(PromotionStatus.PUBLISHED);
		promotionRepository.save(promotion);
	}

	private Promotion findById(Long id) {
		return promotionRepository.findByPromotionId(id)
				.orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
	}

	private Set<Movies> resolveMovies(Set<Long> movieIds) {
		if (movieIds == null || movieIds.isEmpty())
			return new HashSet<>();
		Set<Movies> movies = new HashSet<>();
		for (Long movieId : movieIds) {
			movies.add(movieRepository.findByMovieId(movieId)
					.orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND)));
		}
		return movies;
	}
}
