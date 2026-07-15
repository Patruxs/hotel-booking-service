package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "api_actions")
public class ApiAction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 160)
    @NotNull
    @Column(name = "key", nullable = false, length = 160)
    private String key;

    @Size(max = 12)
    @NotNull
    @Column(name = "http_method", nullable = false, length = 12)
    private String httpMethod;

    @Size(max = 255)
    @NotNull
    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @NotNull
    @Column(name = "is_system", nullable = false)
    private Boolean system = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "action")
    private Set<ApiActionPolicy> policies = new LinkedHashSet<>();
}
