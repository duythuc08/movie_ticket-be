package com.example.movie_ticket_be.user.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.user.dto.request.UserUpdateRequest;
import com.example.movie_ticket_be.user.dto.request.UsersCreationRequest;
import com.example.movie_ticket_be.user.dto.response.UserClientRespone;
import com.example.movie_ticket_be.user.dto.response.UserMenuRespone;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.example.movie_ticket_be.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {
    UserService userService;

    @PostMapping
    ApiResponse<UsersRespone> createUser(@RequestBody @Valid UsersCreationRequest request){
        return ApiResponse.<UsersRespone>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UsersRespone>> getUsers(){
        return ApiResponse.<List<UsersRespone>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("{userId}")
    ApiResponse<UsersRespone> getUser(@PathVariable("userId") String userId){
        return ApiResponse.<UsersRespone>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserClientRespone> getMyInfo(){
        return ApiResponse.<UserClientRespone>builder()
                .result(userService.getMyInfo())
                .build();
    }
    @GetMapping("/myMenu")
    ApiResponse<UserMenuRespone> getUserMenu(){
        return ApiResponse.<UserMenuRespone>builder()
                .result(userService.getUserMenu())
                .build();
    }

    @PutMapping("/myInfo")
    ApiResponse<UserClientRespone> updateMyInfo(@RequestBody UserUpdateRequest request){
        return ApiResponse.<UserClientRespone>builder()
                .result(userService.updateMyInfo(request))
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UsersRespone> updateUser(@PathVariable String userId ,@RequestBody @Valid UserUpdateRequest request){

        return ApiResponse.<UsersRespone>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("{userId}")
    ApiResponse<String> deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return  ApiResponse.<String>builder()
                .result("User has been deleted")
                .build();
    }
}
