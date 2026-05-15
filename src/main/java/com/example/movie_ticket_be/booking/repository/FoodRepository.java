package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.Foods;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Foods, String> {
    boolean existsByName(String name);

    Optional<Foods> findByFoodId(Long foodId);
}
