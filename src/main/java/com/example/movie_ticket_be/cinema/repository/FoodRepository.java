package com.example.movie_ticket_be.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.movie_ticket_be.cinema.entity.Foods;
import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.core.enums.EntityStatus;

public interface FoodRepository extends JpaRepository<Foods, Long>, JpaSpecificationExecutor<Foods> {

	boolean existsByNameAndCinema_CinemaId(String name, Long cinemaId);

	boolean existsByNameAndCinema_CinemaIdAndFoodIdNot(String name, Long cinemaId, Long foodId);

	Optional<Foods> findByFoodId(Long foodId);

	List<Foods> findByCinema_CinemaIdAndEntityStatusAndFoodStatus(Long cinemaId, EntityStatus entityStatus,
			FoodStatus foodStatus);
}
