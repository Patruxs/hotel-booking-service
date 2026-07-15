package org.example.hotelbookingservice.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AuthResponses {
    private AuthResponses() {
    }

    public record RegistrationResponse(UUID userId, String email, boolean emailVerified) {
    }

    public record TokenResponse(
            @Schema(description = "JWT access token used in Authorization bearer headers", example = "eyJhbGciOiJIUzI1NiJ9...")
            String accessToken,
            @Schema(description = "JWT ID for the issued access token", example = "7b4d0f08-7a9f-4c92-9c3a-22c6405e68af")
            String jti,
            @Schema(description = "Token type", example = "Bearer")
            String tokenType) {
    }

    @Schema(name = "AuthTokenApiResponse", description = "Successful authentication response")
    public record TokenApiResponse(
            @Schema(example = "200")
            int status,
            @Schema(example = "true")
            boolean success,
            @Schema(example = "User logged in successfully")
            String message,
            TokenResponse data,
            Instant timestamp) {
    }

    public record RoleDto(UUID id, String name, String displayName, String description, boolean isSystem, List<RolePermissionDto> permissions) {
    }

    public record PermissionDto(UUID id, String key, String name, String description, boolean isSystem) {
    }

    public record RolePermissionDto(UUID permissionId, PermissionDto permission) {
    }

    public record ActionPolicyDto(UUID id, UUID permissionId, String scope, String mode, PermissionDto permission) {
    }

    public record ActionDto(UUID id, String key, String httpMethod, String path, String description, boolean enabled, boolean isSystem, String mode, List<ActionPolicyDto> policies) {
    }

    public record CurrentUserResponse(UUID id, String firstName, String lastName, String email, String phone, LocalDate dob, LocalDate dateOfBirth, List<RoleDto> roles, boolean emailVerified, Instant createdAt, Instant updatedAt, AvatarDto avatar, List<String> allowedActions) {
    }

    public record AvatarDto(String url) {
    }

    public record AuthSessionResponse(String jti, String provider, String ip, String userAgent, Instant createdAt, Instant expiresAt, Instant revokedAt) {
    }

    public record UserListItem(UUID id, String firstName, String lastName, String email, boolean emailVerified, Instant createdAt, Instant updatedAt, List<RoleDto> roles) {
    }

    public record PageResponse<T>(List<T> data, PageMeta meta) {
    }

    public record PageMeta(int limit, int offset, long total) {
    }
}
