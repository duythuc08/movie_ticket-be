package com.example.movie_ticket_be.user.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.request.UsersCreationRequest;
import com.example.movie_ticket_be.user.dto.response.UserClientRespone;
import com.example.movie_ticket_be.user.dto.response.UserMenuRespone;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    private final RoleRepository roleRepository;
    UserRepository userRepository;
    UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UsersRespone createUser(UsersCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        Role role = roleRepository.save(Role.builder().name(Roles.USER.name()).build());
        var roles = new HashSet<Role>();
        roles.add(role);

        Users newUsers = userMapper.toUsers(request);
        newUsers.setPassword(passwordEncoder.encode(newUsers.getPassword()));
        newUsers.setRole(roles);
        newUsers.setUserStatus(UserStatus.ACTIVE);
        return  userMapper.toUsersRespone(userRepository.save(newUsers));
    }

    public UserClientRespone getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Users user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserClientRespone(user);
    }

    public UserMenuRespone getUserMenu() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Users user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserMenuRespone(user);
    }

    public UserClientRespone updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        Users user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getFirstname() != null) user.setFirstname(request.getFirstname());
        if (request.getLastname() != null) user.setLastname(request.getLastname());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getBirthday() != null) user.setBirthday(request.getBirthday());

        return userMapper.toUserClientRespone(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UsersRespone> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUsersRespone)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsersRespone getUser(String id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUsersRespone(user);
    }

    @PreAuthorize("#userId == authentication.name or hasAuthority('ADMIN')")
    public UsersRespone updateUser(String userId, UserUpdateRequest request) {
        Users newUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateUser(newUser, request);

        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        newUser.setRole(new HashSet<>(roles));

        return userMapper.toUsersRespone(userRepository.save(newUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void changUserStatus(String userId, UserStatus status) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setUserStatus(status);
        userRepository.save(user);
    }
}
