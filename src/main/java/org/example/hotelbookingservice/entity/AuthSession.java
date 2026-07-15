package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "auth_sessions")
public class AuthSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "account_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, insertable = false, updatable = false)
    private User user;

    @Size(max = 120)
    @NotNull
    @Column(name = "jti", nullable = false, length = 120)
    private String jti;

    @Size(max = 255)
    @NotNull
    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshHash;

    @Size(max = 32)
    @NotNull
    @Column(name = "provider", nullable = false, length = 32)
    private String provider = "LOCAL";

    @Column(name = "user_agent")
    private String userAgent;

    @Size(max = 64)
    @Column(name = "ip_address", length = 64)
    private String ip;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
