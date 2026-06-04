package com.example.movie_ticket_be.user.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.response.LoyaltyPointsHistoryResponse;
import com.example.movie_ticket_be.user.dto.response.UserClientRespone;
import com.example.movie_ticket_be.user.dto.response.UserMenuRespone;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.mapper.LoyaltyPointsHistoryMapper;
import com.example.movie_ticket_be.user.mapper.UserMapper;
import com.example.movie_ticket_be.user.repository.LoyaltyPointsHistoryRepository;
import com.example.movie_ticket_be.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

	UserRepository userRepository;
	UserMapper userMapper;
	LoyaltyPointsHistoryRepository loyaltyPointsHistoryRepository;
	LoyaltyPointsHistoryMapper loyaltyPointsHistoryMapper;

	public UserClientRespone getMyInfo() {
		return userMapper.toUserClientRespone(getCurrentUser());
	}

	public UserMenuRespone getUserMenu() {
		return userMapper.toUserMenuRespone(getCurrentUser());
	}

	public UserClientRespone updateMyInfo(UserUpdateRequest request) {
		Users user = getCurrentUser();
		if (request.getFirstname() != null)
			user.setFirstname(request.getFirstname());
		if (request.getLastname() != null)
			user.setLastname(request.getLastname());
		if (request.getPhoneNumber() != null)
			user.setPhoneNumber(request.getPhoneNumber());
		if (request.getBirthday() != null)
			user.setBirthday(request.getBirthday());
		return userMapper.toUserClientRespone(userRepository.save(user));
	}

	public Page<LoyaltyPointsHistoryResponse> getMyLoyaltyHistory(Pageable pageable) {
		String userId = getCurrentUser().getUserId();
		return loyaltyPointsHistoryRepository.findByUser_UserId(userId, pageable)
				.map(loyaltyPointsHistoryMapper::toResponse);
	}

	private Users getCurrentUser() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
	}
}
