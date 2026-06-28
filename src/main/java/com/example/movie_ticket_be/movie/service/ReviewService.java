package com.example.movie_ticket_be.movie.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.repository.OrderTicketRepository;
import com.example.movie_ticket_be.recommendation.dto.request.ActivityLogRequest;
import com.example.movie_ticket_be.recommendation.enums.ActionType;
import com.example.movie_ticket_be.recommendation.service.UserActivityLogService;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.ReviewRequest;
import com.example.movie_ticket_be.movie.dto.response.MovieReviewPageResponse;
import com.example.movie_ticket_be.movie.dto.response.ReviewResponse;
import com.example.movie_ticket_be.movie.dto.response.UnreviewedMovieResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.movie.mapper.ReviewMapper;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import com.example.movie_ticket_be.movie.entity.ReviewInteractions;
import com.example.movie_ticket_be.movie.repository.ReviewInteractionRepository;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.enums.InteractionType;
import com.example.movie_ticket_be.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    UserRepository userRepository;
    MovieRepository movieRepository;
    OrderTicketRepository orderTicketRepository;
    ReviewInteractionRepository interactionRepository;
    UserActivityLogService userActivityLogService;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Movies movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        if (reviewRepository.existsByUsersAndMovies(user, movie)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        long completedBookings = orderTicketRepository.countCompletedBookingByUserAndMovie(
                user, movie, LocalDateTime.now(), List.of(OrderStatus.PAID, OrderStatus.USED));
        if (completedBookings == 0) {
            throw new AppException(ErrorCode.CANNOT_REVIEW_UNFINISHED_SHOWTIME);
        }

        Reviews review = reviewMapper.toReview(request);
        review.setUsers(user);
        review.setMovies(movie);
        review.setComment(request.getComment());
        Reviews saved = reviewRepository.save(review);

        userActivityLogService.logInternal(ActivityLogRequest.builder()
                .actionType(ActionType.WRITE_REVIEW)
                .movieId(movie.getMovieId())
                .metadata(java.util.Map.of(
                        "reviewId", saved.getReviewId(),
                        "rating", request.getRating()
                ))
                .build());

        return reviewMapper.toReviewResponse(saved);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getUsers().getUserId().equals(user.getUserId())) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNED);
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setReviewStatus(ReviewStatus.PENDING);
        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    public MovieReviewPageResponse getReviewsByMovie(Long movieId, int page, int size, Integer rating) {
        Movies movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reviews> reviewPage = (rating != null)
                ? reviewRepository.findByMoviesAndReviewStatusAndRating(movie, ReviewStatus.APPROVED, rating, pageRequest)
                : reviewRepository.findByMoviesAndReviewStatus(movie, ReviewStatus.APPROVED, pageRequest);

        // Build rating distribution from all approved reviews (not just current page)
        Map<Integer, Long> distribution = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        reviewRepository.countGroupByRating(movie, ReviewStatus.APPROVED)
                .forEach(row -> distribution.put(((Number) row[0]).intValue(), (Long) row[1]));

        long totalReviews = distribution.values().stream().mapToLong(Long::longValue).sum();
        double averageRating = totalReviews == 0 ? 0.0
                : distribution.entrySet().stream()
                        .mapToDouble(e -> e.getKey() * e.getValue())
                        .sum() / totalReviews;

        Users currentUser = getCurrentUser();
        List<ReviewResponse> content;
        if (currentUser == null) {
            content = reviewPage.getContent().stream()
                    .map(reviewMapper::toReviewResponse)
                    .collect(Collectors.toList());
        } else {
            List<Reviews> reviews = reviewPage.getContent();
            Map<Long, InteractionType> interactionMap = interactionRepository
                    .findByUsersAndReviewsInAndIsActiveTrue(currentUser, reviews)
                    .stream()
                    .collect(Collectors.toMap(
                            i -> i.getReviews().getReviewId(),
                            ReviewInteractions::getInteractionType));
            content = reviews.stream().map(r -> {
                ReviewResponse response = reviewMapper.toReviewResponse(r);
                InteractionType type = interactionMap.get(r.getReviewId());
                response.setLikedByMe(type == InteractionType.LIKE);
                response.setDislikedByMe(type == InteractionType.DISLIKE);
                return response;
            }).collect(Collectors.toList());
        }

        return MovieReviewPageResponse.builder()
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .totalReviews(totalReviews)
                .ratingDistribution(distribution)
                .content(content)
                .currentPage(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .last(reviewPage.isLast())
                .build();
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    public List<UnreviewedMovieResponse> getRecentUnreviewedMovie() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();

        List<Movies> unreviewedMovies = movieRepository.findUnreviewedActiveMovies(
                currentUser.getUserId(),
                now,
                PageRequest.of(0, 10) // fetch up to 10 unreviewed movies
        );

        return unreviewedMovies.stream()
                .map(movie -> UnreviewedMovieResponse.builder()
                        .movieId(movie.getMovieId())
                        .movieName(movie.getTitle())
                        .posterUrl(movie.getPosterUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
