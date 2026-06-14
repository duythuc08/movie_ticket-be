package com.example.movie_ticket_be.movie.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.example.movie_ticket_be.movie.dto.request.ReviewRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminReviewResponse;
import com.example.movie_ticket_be.movie.dto.response.ReviewResponse;
import com.example.movie_ticket_be.movie.entity.Reviews;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "comment", target = "comment")
    @Mapping(target = "likeCount", constant = "0")
    @Mapping(target = "dislikeCount", constant = "0")
    @Mapping(target = "reviewStatus", constant = "APPROVED")
    Reviews toReview(ReviewRequest request);

    @Mapping(source = "movies.movieId", target = "movieId")
    @Mapping(source = "users.userId", target = "userId")
    @Mapping(source = "users.username", target = "username")
    @Mapping(target = "fullName", expression = "java((review.getUsers().getFirstname() != null ? review.getUsers().getFirstname() : \"\") + \" \" + (review.getUsers().getLastname() != null ? review.getUsers().getLastname() : \"\"))")
    @Mapping(target = "isLikedByMe", ignore = true)
    @Mapping(target = "isDislikedByMe", ignore = true)
    ReviewResponse toReviewResponse(Reviews review);

    @Mapping(source = "movies.movieId", target = "movieId")
    @Mapping(source = "movies.title", target = "movieTitle")
    @Mapping(source = "users.userId", target = "userId")
    @Mapping(source = "users.username", target = "username")
    @Mapping(target = "fullName", expression = "java((review.getUsers().getFirstname() != null ? review.getUsers().getFirstname() : \"\") + \" \" + (review.getUsers().getLastname() != null ? review.getUsers().getLastname() : \"\"))")
    AdminReviewResponse toAdminReviewResponse(Reviews review);
}
