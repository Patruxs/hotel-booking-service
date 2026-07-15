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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contact_messages")
public class ContactMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private User account;

    @Column(name = "account_id", insertable = false, updatable = false)
    private UUID accountId;

    @Size(max = 120)
    @NotNull
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 320)
    @NotNull
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @Size(max = 180)
    @Column(name = "subject", length = 180)
    private String subject;

    @NotNull
    @Column(name = "message", nullable = false)
    private String message;

    @Size(max = 32)
    @NotNull
    @Column(name = "status", nullable = false, length = 32)
    private String status = "NEW";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by_account_id")
    private User handledBy;

    @Column(name = "handled_by_account_id", insertable = false, updatable = false)
    private UUID handledByAccountId;

    @Column(name = "note")
    private String note;

    @Size(max = 64)
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
