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
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 64)
    @NotNull
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Size(max = 120)
    @NotNull
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "is_system", nullable = false)
    private Boolean system = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "role")
    private Set<UserRole> userRoles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "role")
    private Set<RolePermission> rolePermissions = new LinkedHashSet<>();

    public UUID getUuid() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
        if (displayName == null || displayName.isBlank()) {
            this.displayName = name;
        }
    }
}
