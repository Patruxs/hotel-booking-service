package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.auth.LoginRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.response.LoginResponse;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.hotelbookingservice.api.AuthApi;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController implements AuthApi {

    IUserService userService;

    @Override
    public ApiResponse<UserResponse> registerUser(@RequestBody RegisterRequest request) {
        return ApiResponse.<UserResponse>builder().status(201).message("User created successfully").data(userService.registerUser(request)).build();
    }

    @Override
    public ApiResponse<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        return ApiResponse.<LoginResponse>builder().status(200).message("User logged in successfully").data(userService.loginUser(request)).build();
    }

    @Override
    public ApiResponse<Void> logout() {
        userService.logout();
        return ApiResponse.<Void>builder().status(200).message("Logged out successfully").build();
    }
}
