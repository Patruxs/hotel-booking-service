package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "amenities")
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Size(max = 120)
    @NotNull
    @Column(name = "key", nullable = false, length = 120)
    private String key;

    @Size(max = 120)
    @Column(name = "icon_key", length = 120)
    private String iconKey;

    @Size(max = 120)
    @NotNull
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 64)
    @NotNull
    @Column(name = "type", nullable = false, length = 64)
    private String type;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull
    @Column(name = "is_system", nullable = false)
    private Boolean system = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "amenity")
    private Set<Roomamenity> roomAmenities = new LinkedHashSet<>();

    public Integer getId() {
        return id == null ? null : id.hashCode();
    }

    public UUID getUuid() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id == null ? null : new UUID(0L, id.longValue());
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
        if (name != null && (key == null || key.isBlank())) {
            this.key = name.trim().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
        }
    }
}
