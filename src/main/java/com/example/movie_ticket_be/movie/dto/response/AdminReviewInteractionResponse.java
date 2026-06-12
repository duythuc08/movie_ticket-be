package com.example.movie_ticket_be.movie.dto.response;

import com.example.movie_ticket_be.user.enums.InteractionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminReviewInteractionResponse {
    Long reviewInteractionId;
    String userId;
    String userName;
    Long reviewId;
    InteractionType interactionType;
}
