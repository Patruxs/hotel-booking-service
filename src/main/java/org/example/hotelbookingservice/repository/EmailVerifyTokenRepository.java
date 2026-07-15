package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.EmailVerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerifyTokenRepository extends JpaRepository<EmailVerifyToken, UUID> {
    Optional<EmailVerifyToken> findByTokenHash(String tokenHash);
}
