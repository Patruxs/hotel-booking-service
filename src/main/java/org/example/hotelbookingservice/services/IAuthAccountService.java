package org.example.hotelbookingservice.services;

import jakarta.servlet.http.HttpServletRequest;
import org.example.hotelbookingservice.dto.request.auth.*;
import org.example.hotelbookingservice.dto.request.user.AdminUserUpdateRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IAuthAccountService {
    RegistrationResponse register(RegisterRequest request);
    TokenIssue login(LoginRequest request, HttpServletRequest httpRequest);
    TokenIssue refresh(String refreshToken, HttpServletRequest request);
    void logout(String refreshToken);
    void logoutAll();
    List<AuthSessionResponse> sessions();
    void verifyEmail(String token);
    void resendVerification(EmailRequest request);
    void forgotPassword(EmailRequest request);
    void resetPassword(ResetPasswordRequest request);
    CurrentUserResponse currentUser();
    CurrentUserResponse updateCurrentUser(UserUpdateRequest request);
    void changePassword(ChangePasswordRequest request);
     PageResponse<UserListItem> users(int limit, int offset);
     UserListItem updateUser(UUID userId, AdminUserUpdateRequest request);
     void assignRoles(RoleAssignmentRequest request);
    PageResponse<RoleDto> roles(int limit, int offset);
    PageResponse<PermissionDto> permissions(int limit, int offset);
    PageResponse<ActionDto> actions(int limit, int offset);
    RoleDto createRole(RoleMutationRequest request);
    RoleDto updateRole(UUID roleId, RoleMutationRequest request);
    void deleteRole(UUID roleId);
    PermissionDto createPermission(PermissionMutationRequest request);
    PermissionDto updatePermission(UUID permissionId, PermissionMutationRequest request);
    void deletePermission(UUID permissionId);
    RoleDto replaceRolePermissions(UUID roleId, PermissionAssignmentRequest request);
    ActionDto replaceActionPermissions(UUID actionId, PermissionAssignmentRequest request);
    UUID currentAccountId();

    record TokenIssue(TokenResponse response, String refreshToken, Instant refreshExpiresAt) {
    }
}
