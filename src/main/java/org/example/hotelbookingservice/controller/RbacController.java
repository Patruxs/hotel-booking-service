package org.example.hotelbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.api.RbacApi;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.auth.PermissionAssignmentRequest;
import org.example.hotelbookingservice.dto.request.auth.PermissionMutationRequest;
import org.example.hotelbookingservice.dto.request.auth.RoleAssignmentRequest;
import org.example.hotelbookingservice.dto.request.auth.RoleMutationRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.ActionDto;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.PageResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.PermissionDto;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.RoleDto;
import org.example.hotelbookingservice.services.IAuthAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RbacController implements RbacApi {
    private final IAuthAccountService authAccountService;

    @Override
    public ApiResponse<PageResponse<RoleDto>> roles(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.<PageResponse<RoleDto>>builder().status(200).message("Success").data(authAccountService.roles(limit, offset)).build();
    }

    @Override
    public ApiResponse<Void> assignRoles(@RequestBody @Valid RoleAssignmentRequest request) {
        authAccountService.assignRoles(request);
        return ApiResponse.<Void>builder().status(200).message("Roles assigned successfully").build();
    }

    @Override
    public ApiResponse<RoleDto> createRole(@RequestBody @Valid RoleMutationRequest request) {
        return ApiResponse.<RoleDto>builder().status(201).message("Role created successfully").data(authAccountService.createRole(request)).build();
    }

    @Override
    public ApiResponse<RoleDto> updateRole(@PathVariable UUID roleId, @RequestBody @Valid RoleMutationRequest request) {
        return ApiResponse.<RoleDto>builder().status(200).message("Role updated successfully").data(authAccountService.updateRole(roleId, request)).build();
    }

    @Override
    public ApiResponse<Void> deleteRole(@PathVariable UUID roleId) {
        authAccountService.deleteRole(roleId);
        return ApiResponse.<Void>builder().status(200).message("Role deleted successfully").build();
    }

    @Override
    public ApiResponse<RoleDto> assignRolePermissions(@PathVariable UUID roleId, @RequestBody @Valid PermissionAssignmentRequest request) {
        return ApiResponse.<RoleDto>builder().status(200).message("Role permissions updated successfully").data(authAccountService.replaceRolePermissions(roleId, request)).build();
    }

    @Override
    public ApiResponse<PageResponse<PermissionDto>> permissions(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.<PageResponse<PermissionDto>>builder().status(200).message("Success").data(authAccountService.permissions(limit, offset)).build();
    }

    @Override
    public ApiResponse<PermissionDto> createPermission(@RequestBody @Valid PermissionMutationRequest request) {
        return ApiResponse.<PermissionDto>builder().status(201).message("Permission created successfully").data(authAccountService.createPermission(request)).build();
    }

    @Override
    public ApiResponse<PermissionDto> updatePermission(@PathVariable UUID permissionId, @RequestBody @Valid PermissionMutationRequest request) {
        return ApiResponse.<PermissionDto>builder().status(200).message("Permission updated successfully").data(authAccountService.updatePermission(permissionId, request)).build();
    }

    @Override
    public ApiResponse<Void> deletePermission(@PathVariable UUID permissionId) {
        authAccountService.deletePermission(permissionId);
        return ApiResponse.<Void>builder().status(200).message("Permission deleted successfully").build();
    }

    @Override
    public ApiResponse<PageResponse<ActionDto>> actions(@RequestParam(defaultValue = "20") int limit, @RequestParam(defaultValue = "0") int offset) {
        return ApiResponse.<PageResponse<ActionDto>>builder().status(200).message("Success").data(authAccountService.actions(limit, offset)).build();
    }

    @Override
    public ApiResponse<ActionDto> assignActionPermissions(@PathVariable UUID actionId, @RequestBody @Valid PermissionAssignmentRequest request) {
        return ApiResponse.<ActionDto>builder().status(200).message("Action permissions updated successfully").data(authAccountService.replaceActionPermissions(actionId, request)).build();
    }
}
