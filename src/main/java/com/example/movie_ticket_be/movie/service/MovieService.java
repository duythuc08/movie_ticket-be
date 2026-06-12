package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.dto.response.PagedMovieResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.movie.mapper.MovieMapper;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.repository.UserRepository;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {
	MovieRepository movieRepository;
	MovieMapper movieMapper;
	UserRepository userRepository;
	ReviewRepository reviewRepository;

	private Users getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
			return null;
		}
		return userRepository.findByUsername(auth.getName()).orElse(null);
	}

	public MovieResponse getMovieById(Long movieId) {
		Movies movie = movieRepository.findByMovieId(movieId)
				.orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
		MovieResponse response = movieMapper.toMovieResponse(movie);
		
		Users currentUser = getCurrentUser();
		if (currentUser != null) {
			boolean hasReviewed = reviewRepository.existsByUsersAndMovies(currentUser, movie);
			response.setHasReviewed(hasReviewed);
		} else {
			response.setHasReviewed(false);
		}
		
		return response;
	}
	public List<MovieResponse> getMovies() {
		return movieRepository.findAll().stream().map(movieMapper::toMovieResponse).toList();
	}

	public List<MovieResponse> getMoviesShowing() {
		return movieRepository.findByEntityStatusAndMovieStatus(EntityStatus.ACTIVE, MovieStatus.NOW_SHOWING).stream()
				.map(movieMapper::toMovieResponse).toList();
	}
	public List<MovieResponse> getMoviesComingSoon() {
		return movieRepository.findByEntityStatusAndMovieStatus(EntityStatus.ACTIVE, MovieStatus.COMING_SOON).stream()
				.map(movieMapper::toMovieResponse).toList();
	}

	public List<MovieResponse> getMoviesShowingPaged(int page, int size) {
		return movieRepository
				.findByEntityStatusAndMovieStatus(EntityStatus.ACTIVE, MovieStatus.NOW_SHOWING,
						PageRequest.of(page, size, Sort.by("movieId").descending()))
				.getContent().stream().map(movieMapper::toMovieResponse).toList();
	}

	public List<MovieResponse> getMoviesComingSoonPaged(int page, int size) {
		return movieRepository
				.findByEntityStatusAndMovieStatus(EntityStatus.ACTIVE, MovieStatus.COMING_SOON,
						PageRequest.of(page, size, Sort.by("movieId").descending()))
				.getContent().stream().map(movieMapper::toMovieResponse).toList();
	}

	private PagedMovieResponse buildPagedResponse(MovieStatus status, int page, int size) {
		Page<Movies> result = movieRepository.findByEntityStatusAndMovieStatus(EntityStatus.ACTIVE, status,
				PageRequest.of(page, size, Sort.by("createdAt").descending()));
		return PagedMovieResponse.builder()
				.content(result.getContent().stream().map(movieMapper::toMovieResponse).toList()).currentPage(page)
				.pageSize(size).totalPages(result.getTotalPages()).totalElements(result.getTotalElements()).build();
	}

	public PagedMovieResponse getShowingMoviesPagedResponse(int page, int size) {
		return buildPagedResponse(MovieStatus.NOW_SHOWING, page, size);
	}

	public PagedMovieResponse getComingSoonMoviesPagedResponse(int page, int size) {
		return buildPagedResponse(MovieStatus.COMING_SOON, page, size);
	}

}