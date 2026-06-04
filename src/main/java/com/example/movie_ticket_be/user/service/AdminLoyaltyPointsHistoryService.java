package com.example.movie_ticket_be.user.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.response.LoyaltyPointsHistoryResponse;
import com.example.movie_ticket_be.user.mapper.LoyaltyPointsHistoryMapper;
import com.example.movie_ticket_be.user.repository.LoyaltyPointsHistoryRepository;
import com.example.movie_ticket_be.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminLoyaltyPointsHistoryService {

    LoyaltyPointsHistoryRepository loyaltyPointsHistoryRepository;
    LoyaltyPointsHistoryMapper loyaltyPointsHistoryMapper;
    UserRepository userRepository;

    public Page<LoyaltyPointsHistoryResponse> getHistoriesByUserId(String userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return loyaltyPointsHistoryRepository
                .findByUser_UserId(userId, pageable)
                .map(loyaltyPointsHistoryMapper::toResponse);
    }
}
