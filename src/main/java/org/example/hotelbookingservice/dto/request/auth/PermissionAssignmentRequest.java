package org.example.hotelbookingservice.dto.request.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PermissionAssignmentRequest {
    @NotNull
    private List<UUID> permissionIds;
}
