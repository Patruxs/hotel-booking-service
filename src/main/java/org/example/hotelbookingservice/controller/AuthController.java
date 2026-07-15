package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.auth.EmailRequest;
import org.example.hotelbookingservice.dto.request.auth.LoginRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.request.auth.ResetPasswordRequest;
import org.example.hotelbookingservice.dto.request.auth.TokenRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.AuthSessionResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.RegistrationResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.TokenResponse;
import org.example.hotelbookingservice.services.IAuthAccountService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.example.hotelbookingservice.api.AuthApi;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController implements AuthApi {

    IAuthAccountService authAccountService;

    @Override
    public ApiResponse<RegistrationResponse> registerUser(@RequestBody RegisterRequest request) {
        return ApiResponse.<RegistrationResponse>builder().status(201).message("Account created. Please verify your email.").data(authAccountService.register(request)).build();
    }

    @Override
    public ApiResponse<TokenResponse> loginUser(@RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        IAuthAccountService.TokenIssue tokenIssue = authAccountService.login(request, httpRequest);
        setRefreshCookie(response, tokenIssue);
        return ApiResponse.<TokenResponse>builder().status(200).message("User logged in successfully").data(tokenIssue.response()).build();
    }

    @Override
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authAccountService.logout(readRefreshToken(request));
        clearRefreshCookie(response);
        return ApiResponse.<Void>builder().status(200).message("Logged out successfully").build();
    }

    @Override
    public ApiResponse<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        IAuthAccountService.TokenIssue tokenIssue = authAccountService.refresh(readRefreshToken(request), request);
        setRefreshCookie(response, tokenIssue);
        return ApiResponse.<TokenResponse>builder().status(200).message("Token refreshed successfully").data(tokenIssue.response()).build();
    }

    @Override
    public ApiResponse<Void> logoutAll(HttpServletResponse response) {
        authAccountService.logoutAll();
        clearRefreshCookie(response);
        return ApiResponse.<Void>builder().status(200).message("All sessions logged out successfully").build();
    }

    @Override
    public ApiResponse<List<AuthSessionResponse>> sessions() {
        return ApiResponse.<List<AuthSessionResponse>>builder().status(200).message("Success").data(authAccountService.sessions()).build();
    }

    @Override
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid TokenRequest request) {
        authAccountService.verifyEmail(request.getToken());
        return ApiResponse.<Void>builder().status(200).message("Email verified successfully").build();
    }

    @Override
    public ApiResponse<Void> verifyEmailQuery(@RequestParam String token) {
        authAccountService.verifyEmail(token);
        return ApiResponse.<Void>builder().status(200).message("Email verified successfully").build();
    }

    @Override
    public ApiResponse<Void> resend(@RequestBody @Valid EmailRequest request) {
        authAccountService.resendVerification(request);
        return ApiResponse.<Void>builder().status(200).message("If the account is pending, a verification email will be sent.").build();
    }

    @Override
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid EmailRequest request) {
        authAccountService.forgotPassword(request);
        return ApiResponse.<Void>builder().status(200).message("If the account exists, a reset email will be sent.").build();
    }

    @Override
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authAccountService.resetPassword(request);
        return ApiResponse.<Void>builder().status(200).message("Password reset successfully").build();
    }

    private void setRefreshCookie(HttpServletResponse response, IAuthAccountService.TokenIssue tokenIssue) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenIssue.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.between(java.time.Instant.now(), tokenIssue.refreshExpiresAt()))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String readRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

}
