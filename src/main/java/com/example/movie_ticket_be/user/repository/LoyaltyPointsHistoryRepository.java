package com.example.movie_ticket_be.user.repository;

import com.example.movie_ticket_be.user.entity.LoyaltyPointsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoyaltyPointsHistoryRepository extends JpaRepository<LoyaltyPointsHistory, Long> {
    Page<LoyaltyPointsHistory> findByUser_UserId(String userId, Pageable pageable);
}
