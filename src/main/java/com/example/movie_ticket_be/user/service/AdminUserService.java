package com.example.movie_ticket_be.user.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.request.UsersCreationRequest;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.example.movie_ticket_be.user.entity.Role;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.enums.Roles;
import com.example.movie_ticket_be.user.enums.UserStatus;
import com.example.movie_ticket_be.user.mapper.UserMapper;
import com.example.movie_ticket_be.user.repository.RoleRepository;
import com.example.movie_ticket_be.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserService {
	UserRepository userRepository;
	UserMapper userMapper;
	RoleRepository roleRepository;
	PasswordEncoder passwordEncoder;

	public UsersRespone createUser(UsersCreationRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new AppException(ErrorCode.EMAIL_EXISTED);
		}
		Role role = roleRepository.save(Role.builder().name(Roles.USER.name()).build());
		var roles = new HashSet<Role>();
		roles.add(role);
		Users newUsers = userMapper.toUsers(request);
		newUsers.setPassword(passwordEncoder.encode(newUsers.getPassword()));
		newUsers.setRole(roles);
		newUsers.setUserStatus(UserStatus.UNVERIFIED);
		return userMapper.toUsersRespone(userRepository.save(newUsers));
	}

	public Page<UsersRespone> getUsers(Specification<Users> spec, Pageable pageable) {
		return userRepository.findAll(spec, pageable).map(userMapper::toUsersRespone);
	}

	public UsersRespone getUserById(String id) {
		Users user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		return userMapper.toUsersRespone(user);
	}

	public UsersRespone updateUser(String userId, UserUpdateRequest request) {
		Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		userMapper.updateUser(user, request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		var roles = roleRepository.findAllById(request.getRoles());
		user.setRole(new HashSet<>(roles));
		return userMapper.toUsersRespone(userRepository.save(user));
	}

	public void deleteUser(String userId) {
		userRepository.deleteById(userId);
	}

	public void changeStatus(String userId, EntityStatus entityStatus) {
		Users user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		user.setEntityStatus(entityStatus);
		userRepository.save(user);
	}
}
