package com.example.movie_ticket_be.movie.repository;

import com.example.movie_ticket_be.movie.entity.Banner;
import com.example.movie_ticket_be.movie.enums.BannerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner,String> {
    boolean existsByTitle(String name);

    Optional<Banner> findById(Long id);

    Banner findByMovies_MovieId(Long moviesMovieId);
    List<Banner> findAllByOrderByPriorityAsc();
    List<Banner> findByActiveTrueOrderByPriorityAsc();
    List<Banner> findByBannerTypeOrderByPriorityAsc(BannerType bannerType);

}
