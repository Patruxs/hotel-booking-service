package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.user.AdminUserUpdateRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.CreateStaffRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.CurrentUserResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.PageResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.UserListItem;
import org.example.hotelbookingservice.services.IAuthAccountService;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import org.example.hotelbookingservice.api.UserApi;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController implements UserApi {

    IUserService userService;
    IAuthAccountService authAccountService;

    @Override
    public ApiResponse<List<UserResponse>> getAllUser() {
        return ApiResponse.<List<UserResponse>>builder().status(200).message("Success").data(userService.getAllUsers()).build();
    }

    @Override
    public ApiResponse<UserResponse> createStaff(@RequestBody CreateStaffRequest request) {
        return ApiResponse.<UserResponse>builder().status(201).message("Staff account created successfully").data(userService.createStaff(request)).build();
    }

    @Override
    public ApiResponse<UserResponse> updateOwnAccount(@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder().status(200).message("User updated successfully").data(userService.updateOwnAccount(request)).build();
    }

    @Override
    public ApiResponse<Void> deleteOwnAccount() {
        userService.deleteOwnAccount();
        return ApiResponse.<Void>builder().status(200).message("User deleted successfully").build();
    }

    @Override
    public ApiResponse<UserResponse> getOwnAccountDetails() {
        return ApiResponse.<UserResponse>builder().status(200).message("Success").data(userService.getOwnAccountDetails()).build();
    }

    @Override
    public ApiResponse<List<BookingResponse>> getMyBookingHistory() {
        return ApiResponse.<List<BookingResponse>>builder().status(200).message("Success").data(userService.getMyBookingHistory()).build();
    }

    @Override
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authAccountService.changePassword(request);
        return ApiResponse.<Void>builder().status(200).message("Password changed successfully").build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<PageResponse<UserListItem>> listUsers(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.<PageResponse<UserListItem>>builder().status(200).message("Success").data(authAccountService.users(limit, offset)).build();
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.<CurrentUserResponse>builder().status(200).message("Success").data(authAccountService.currentUser()).build();
    }

    @PatchMapping("/me")
    public ApiResponse<CurrentUserResponse> updateMe(@RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<CurrentUserResponse>builder().status(200).message("User updated successfully").data(authAccountService.updateCurrentUser(request)).build();
    }

    @PostMapping("/me/change-password")
    public ApiResponse<Void> changePasswordCanonical(@RequestBody ChangePasswordRequest request) {
        authAccountService.changePassword(request);
        return ApiResponse.<Void>builder().status(200).message("Password changed successfully").build();
    }

    @Override
    public ApiResponse<Void> lockUser(@PathVariable Integer userId) {
        userService.lockUser(userId);
        return ApiResponse.<Void>builder().status(200).message("User locked successfully").build();
    }

    @Override
    public ApiResponse<Void> unlockUser(@PathVariable Integer userId) {
        userService.unlockUser(userId);
        return ApiResponse.<Void>builder().status(200).message("User unlocked successfully").build();
    }

    @Override
    public ApiResponse<UserListItem> updateUser(@PathVariable UUID userId, @RequestBody @Valid AdminUserUpdateRequest request) {
        return ApiResponse.<UserListItem>builder()
                .status(200)
                .message("User updated successfully")
                .data(authAccountService.updateUser(userId, request))
                .build();
    }
}

