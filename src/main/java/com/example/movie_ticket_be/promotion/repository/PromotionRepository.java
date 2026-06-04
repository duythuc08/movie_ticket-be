package com.example.movie_ticket_be.promotion.repository;

import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

	Optional<Promotion> findByPromotionId(Long promotionId);

	Optional<Promotion> findByCode(String code);

	boolean existsByCode(String code);

	List<Promotion> findByStatusAndEndTimeBefore(PromotionStatus status, LocalDateTime now);

	List<Promotion> findByStatus(PromotionStatus status);
}
