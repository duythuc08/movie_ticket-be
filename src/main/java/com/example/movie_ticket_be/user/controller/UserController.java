package com.example.movie_ticket_be.user.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.response.UserClientRespone;
import com.example.movie_ticket_be.user.dto.response.UserMenuRespone;
import com.example.movie_ticket_be.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @GetMapping("/myInfo")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserClientRespone> getMyInfo() {
        return ApiResponse.<UserClientRespone>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @GetMapping("/myMenu")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserMenuRespone> getUserMenu() {
        return ApiResponse.<UserMenuRespone>builder()
                .result(userService.getUserMenu())
                .build();
    }

    @PutMapping("/myInfo")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserClientRespone> updateMyInfo(@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserClientRespone>builder()
                .result(userService.updateMyInfo(request))
                .build();
    }
}
