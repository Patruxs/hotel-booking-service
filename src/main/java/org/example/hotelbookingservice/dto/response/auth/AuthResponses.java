package org.example.hotelbookingservice.dto.response.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AuthResponses {
    private AuthResponses() {
    }

    public record RegistrationResponse(UUID userId, String email, boolean emailVerified) {
    }

    public record TokenResponse(String accessToken, String jti, String tokenType) {
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

    public record CurrentUserResponse(UUID id, String firstName, String lastName, String email, List<RoleDto> roles, boolean emailVerified, Instant createdAt, Instant updatedAt, AvatarDto avatar, List<String> allowedActions) {
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
