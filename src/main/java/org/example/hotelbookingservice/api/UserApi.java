package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.user.AdminUserUpdateRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.CreateStaffRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User management (Personal information, Booking history, Admin)")
public interface UserApi {

    @Operation(summary = "Get list of all users (ADMIN)", description = "Only Admin has access.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in (Invalid token)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied (Not an ADMIN)", content = @Content) })
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<List<UserResponse>> getAllUser();

    @Operation(summary = "Create staff account (ADMIN)", description = "Admin creates accounts for Receptionist (RECEPTIONIST) or Manager (MANAGER).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Staff created successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid data or email already exists", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission required", content = @Content) })
    @PostMapping({"/staff", "/create-staff"})
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<UserResponse> createStaff(@RequestBody @Valid CreateStaffRequest request);

    @Operation(summary = "Update personal information", description = "User updates their own information.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updated successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid data (Wrong phone number, wrong birthdate...)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in", content = @Content) })
    @PutMapping({"/me", "/update"})
    ApiResponse<UserResponse> updateOwnAccount(@RequestBody @Valid UserUpdateRequest request);

    @Operation(summary = "Delete personal account", description = "User deletes their own account.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Deleted successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in", content = @Content) })
    @DeleteMapping({"/me", "/delete"})
    ApiResponse<Void> deleteOwnAccount();

    @Operation(summary = "Get profile info", description = "Get the profile information of the currently logged in user.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found (Token error)", content = @Content) })
    @GetMapping({"/me/profile", "/get-logged-in-profile-info"})
    ApiResponse<UserResponse> getOwnAccountDetails();

    @Operation(summary = "Booking history", description = "Get the list of bookings of the current user.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in", content = @Content) })
    @GetMapping({"/me/bookings", "/get-user-bookings"})
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMIN')")
    ApiResponse<List<BookingResponse>> getMyBookingHistory();

    @Operation(summary = "Change password", description = "Requires the old password to be correct.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Incorrect old password or new password is the same as old password", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in", content = @Content) })
    @PutMapping({"/me/password", "/change-password"})
    ApiResponse<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request);

    @Operation(summary = "Lock user account (ADMIN)", description = "Admin locks a user account.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Locked successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission required", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User does not exist", content = @Content) })
    @PutMapping("/{userId}/lock")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> lockUser(@PathVariable Integer userId);

    @Operation(summary = "Unlock user account (ADMIN)", description = "Admin unlocks a user account.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unlocked successfully"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Admin permission required", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User does not exist", content = @Content) })
    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> unlockUser(@PathVariable Integer userId);

    @Operation(summary = "Update user profile fields (ADMIN)", description = "Admin updates visible user profile fields.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<org.example.hotelbookingservice.dto.response.auth.AuthResponses.UserListItem> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid AdminUserUpdateRequest request
    );
}
