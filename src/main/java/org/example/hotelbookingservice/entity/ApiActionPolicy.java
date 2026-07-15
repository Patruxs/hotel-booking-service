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
@Table(name = "action_policies")
public class ApiActionPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "action_id", nullable = false)
    private UUID actionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false, insertable = false, updatable = false)
    private ApiAction action;

    @NotNull
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false, insertable = false, updatable = false)
    private Permission permission;

    @Size(max = 32)
    @NotNull
    @Column(name = "scope", nullable = false, length = 32)
    private String scope = "GLOBAL";

    @Size(max = 16)
    @NotNull
    @Column(name = "mode", nullable = false, length = 16)
    private String mode = "ANY";

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
