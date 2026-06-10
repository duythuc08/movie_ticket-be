package com.example.movie_ticket_be.movie.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.movie_ticket_be.movie.dto.request.ReviewRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminReviewResponse;
import com.example.movie_ticket_be.movie.dto.response.ReviewResponse;
import com.example.movie_ticket_be.movie.entity.Reviews;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "movies", ignore = true)
    @Mapping(target = "likeCount", constant = "0")
    @Mapping(target = "dislikeCount", constant = "0")
    @Mapping(target = "reviewStatus", constant = "APPROVED")
    @Mapping(target = "reviewId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "entityStatus", ignore = true)
    Reviews toReview(ReviewRequest request);

    @Mapping(source = "movies.movieId", target = "movieId")
    @Mapping(source = "users.userId", target = "userId")
    @Mapping(source = "users.username", target = "username")
    @Mapping(target = "isLikedByMe", ignore = true)
    @Mapping(target = "isDislikedByMe", ignore = true)
    ReviewResponse toReviewResponse(Reviews review);

    @Mapping(source = "movies.movieId", target = "movieId")
    @Mapping(source = "movies.title", target = "movieTitle")
    @Mapping(source = "users.userId", target = "userId")
    @Mapping(source = "users.username", target = "username")
    AdminReviewResponse toAdminReviewResponse(Reviews review);
}
