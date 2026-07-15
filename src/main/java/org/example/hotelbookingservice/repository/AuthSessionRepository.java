package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByJti(String jti);
}
