package com.example.movie_ticket_be.user.mapper;

import com.example.movie_ticket_be.user.dto.response.LoyaltyPointsHistoryResponse;
import com.example.movie_ticket_be.user.entity.LoyaltyPointsHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoyaltyPointsHistoryMapper {

    @Mapping(source = "order.orderId", target = "orderId")
    LoyaltyPointsHistoryResponse toResponse(LoyaltyPointsHistory history);
}
