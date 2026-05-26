package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.Foods;
import com.example.movie_ticket_be.booking.enums.FoodStatus;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Foods, Long> {
    boolean existsByName(String name);

    Optional<Foods> findByFoodId(Long foodId);

    List<Foods> findByEntityStatusAndFoodStatus(EntityStatus entityStatus, FoodStatus foodStatus);
}
