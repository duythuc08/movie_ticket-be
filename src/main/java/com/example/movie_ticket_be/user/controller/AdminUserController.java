package com.example.movie_ticket_be.user.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.request.UsersCreationRequest;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.example.movie_ticket_be.user.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {
    AdminUserService adminUserService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UsersRespone> createUser(@RequestBody @Valid UsersCreationRequest request) {
        return ApiResponse.<UsersRespone>builder()
                .result(adminUserService.createUser(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UsersRespone>> getUsers() {
        return ApiResponse.<List<UsersRespone>>builder()
                .result(adminUserService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UsersRespone> getUser(@PathVariable String userId) {
        return ApiResponse.<UsersRespone>builder()
                .result(adminUserService.getUser(userId))
                .build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UsersRespone> updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UsersRespone>builder()
                .result(adminUserService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteUser(@PathVariable String userId) {
        adminUserService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable String userId) {
        adminUserService.changeStatus(userId, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt người dùng thành công").build();
    }

    @PutMapping("/{userId}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable String userId) {
        adminUserService.changeStatus(userId, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa người dùng thành công").build();
    }
}
