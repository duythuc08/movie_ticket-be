package com.example.movie_ticket_be.promotion.repository;

import com.example.movie_ticket_be.promotion.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion,String> {
    Promotion findByPromotionId(Long promotionId);

    Optional<Promotion> findByCode(String code);


}
