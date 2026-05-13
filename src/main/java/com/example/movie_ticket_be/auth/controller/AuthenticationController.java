package com.example.movie_ticket_be.auth.controller;

import com.example.movie_ticket_be.auth.dto.request.*;
import com.example.movie_ticket_be.auth.dto.response.AuthenticationResponse;
import com.example.movie_ticket_be.auth.dto.response.IntrospectResponse;
import com.example.movie_ticket_be.auth.service.AuthenticationService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectResquest resquest) throws ParseException, JOSEException {
        var result = authenticationService.introspect(resquest);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationResquest resquest){
        var result = authenticationService.authenticate(resquest);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutResquest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .message("Logout successfully!")
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UsersRespone> register(@RequestBody RegisterRequest request) {
        var result = authenticationService.register(request);
        return ApiResponse.<UsersRespone>builder()
                .result(result)
                .message("Đăng ký thành công, vui lòng kiểm tra email để xác thực")
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verifyEmail(@RequestParam("otp") String otp, @RequestParam String email) {
        authenticationService.verifyEmail(otp,email);
        return ApiResponse.<Void>builder()
                .message( "Xác thực thành công" )
                .build();

    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request.getUsername());
        return ApiResponse.<Void>builder()
                .message("OTP đã được gửi tới email")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request,@RequestParam String email) {
        authenticationService.resetPassword(email, request.getNewPassword());
        return ApiResponse.<Void>builder()
                .message("Lấy mật khẩu thành công")
                .build();
    }
    @PostMapping("/resendOTP")
    public ApiResponse<Void> resendOTP(@RequestParam String email) {
        authenticationService.resendOtp(email);
        return ApiResponse.<Void>builder()
                .message("OTP mới đã được gửi, Vui lòng kiểm tra")
                .build();
    }
}
