package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.auth.LoginRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.CreateStaffRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.LoginResponse;
import org.example.hotelbookingservice.dto.response.UserResponse;
import org.example.hotelbookingservice.entity.User;

import java.util.List;

public interface IUserService {

    UserResponse registerUser(RegisterRequest registrationRequest);
    LoginResponse loginUser(LoginRequest loginRequest);
    List<UserResponse> getAllUsers();
    UserResponse getOwnAccountDetails();
    User getCurrentLoggedInUser();
    UserResponse updateOwnAccount(UserUpdateRequest request);
    void deleteOwnAccount();
    List<org.example.hotelbookingservice.dto.response.BookingResponse> getMyBookingHistory();
    void changePassword(ChangePasswordRequest request);
    void lockUser(Integer userId);
    void unlockUser(Integer userId);
    void logout();
    UserResponse createStaff(CreateStaffRequest request);
}

