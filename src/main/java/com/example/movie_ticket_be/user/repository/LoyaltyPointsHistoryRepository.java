package com.example.movie_ticket_be.user.repository;

import com.example.movie_ticket_be.user.entity.LoyaltyPointsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyPointsHistoryRepository extends JpaRepository<LoyaltyPointsHistory, String> {
}
