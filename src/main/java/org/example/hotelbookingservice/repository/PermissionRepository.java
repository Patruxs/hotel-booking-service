package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByKey(String key);
}
