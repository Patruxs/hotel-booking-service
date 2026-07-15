package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.RolePermission;
import org.example.hotelbookingservice.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {
}
