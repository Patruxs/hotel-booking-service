package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.UserRole;
import org.example.hotelbookingservice.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
