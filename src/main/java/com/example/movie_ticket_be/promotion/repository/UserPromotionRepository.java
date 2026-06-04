package com.example.movie_ticket_be.promotion.repository;

import com.example.movie_ticket_be.promotion.entity.UserPromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {

	List<UserPromotion> findByUsers_UserIdAndIsUsedFalse(String userId);

	boolean existsByUsers_UserIdAndPromotion_PromotionId(String userId, Long promotionId);

	Optional<UserPromotion> findByUsers_UserIdAndPromotion_PromotionIdAndIsUsedFalse(String userId, Long promotionId);
}
