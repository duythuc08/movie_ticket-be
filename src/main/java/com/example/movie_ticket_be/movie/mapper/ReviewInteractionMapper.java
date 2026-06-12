package com.example.movie_ticket_be.movie.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.movie_ticket_be.movie.dto.response.AdminReviewInteractionResponse;
import com.example.movie_ticket_be.movie.entity.ReviewInteractions;

@Mapper(componentModel = "spring")
public interface ReviewInteractionMapper {
    @Mapping(target = "userId", source = "users.userId")
    @Mapping(target = "userName", expression = "java(reviewInteraction.getUsers().getFirstname() + ' ' + reviewInteraction.getUsers().getLastname())")
    @Mapping(target = "reviewId", source = "reviews.reviewId")
    AdminReviewInteractionResponse toAdminReviewInteractionResponse(ReviewInteractions reviewInteraction);

    @Mapping(target = "userId", source = "users.userId")
    @Mapping(target = "userName", expression = "java(reviewInteraction.getUsers().getFirstname() + ' ' + reviewInteraction.getUsers().getLastname())")
    @Mapping(target = "reviewId", source = "reviews.reviewId")
    List<AdminReviewInteractionResponse> toAdminReviewInteractionResponse(List<ReviewInteractions> reviewInteractions);
}
