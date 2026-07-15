package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.auth.PermissionAssignmentRequest;
import org.example.hotelbookingservice.dto.request.auth.PermissionMutationRequest;
import org.example.hotelbookingservice.dto.request.auth.RoleAssignmentRequest;
import org.example.hotelbookingservice.dto.request.auth.RoleMutationRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.ActionDto;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.PageResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.PermissionDto;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.RoleDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "RBAC", description = "Role, permission, and action administration")
@SecurityRequirement(name = "bearerAuth")
public interface RbacApi {

    @Operation(summary = "List roles")
    @GetMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PageResponse<RoleDto>> roles(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset);

    @Operation(summary = "Assign roles to a user")
    @PostMapping("/api/v1/roles/assign-to-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> assignRoles(@RequestBody @Valid RoleAssignmentRequest request);

    @Operation(summary = "Create role")
    @PostMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoleDto> createRole(@RequestBody @Valid RoleMutationRequest request);

    @Operation(summary = "Update role")
    @PatchMapping("/api/v1/roles/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoleDto> updateRole(@PathVariable UUID roleId, @RequestBody @Valid RoleMutationRequest request);

    @Operation(summary = "Delete role")
    @DeleteMapping("/api/v1/roles/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deleteRole(@PathVariable UUID roleId);

    @Operation(summary = "Replace role permissions")
    @PostMapping("/api/v1/roles/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<RoleDto> assignRolePermissions(@PathVariable UUID roleId, @RequestBody @Valid PermissionAssignmentRequest request);

    @Operation(summary = "List permissions")
    @GetMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PageResponse<PermissionDto>> permissions(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset);

    @Operation(summary = "Create permission")
    @PostMapping("/api/v1/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PermissionDto> createPermission(@RequestBody @Valid PermissionMutationRequest request);

    @Operation(summary = "Update permission")
    @PatchMapping("/api/v1/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PermissionDto> updatePermission(@PathVariable UUID permissionId, @RequestBody @Valid PermissionMutationRequest request);

    @Operation(summary = "Delete permission")
    @DeleteMapping("/api/v1/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<Void> deletePermission(@PathVariable UUID permissionId);

    @Operation(summary = "List actions")
    @GetMapping("/api/v1/actions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<PageResponse<ActionDto>> actions(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset);

    @Operation(summary = "Replace action permissions")
    @PatchMapping("/api/v1/actions/{actionId}/permissions")
    @PreAuthorize("hasAuthority('ADMIN')")
    ApiResponse<ActionDto> assignActionPermissions(@PathVariable UUID actionId, @RequestBody @Valid PermissionAssignmentRequest request);
}
