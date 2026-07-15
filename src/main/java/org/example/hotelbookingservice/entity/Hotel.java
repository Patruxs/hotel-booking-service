package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Size(max = 160)
    @NotNull
    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Size(max = 180)
    @NotNull
    @Column(name = "slug", nullable = false, length = 180)
    private String slug;

    @Column(name = "description")
    private String description;

    @Column(name = "address")
    private String address;

    @Size(max = 120)
    @Column(name = "city", length = 120)
    private String location;

    @Size(max = 120)
    @NotNull
    @Column(name = "country", nullable = false, length = 120)
    private String country = "Vietnam";

    @Size(max = 320)
    @Column(name = "email", length = 320)
    private String email;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @NotNull
    @Column(name = "status", nullable = false, length = 32)
    private String status = "DRAFT";

    @Column(name = "star_rating")
    private BigDecimal starRating;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User user;

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Hotelamenity> hotelAmenities = new LinkedHashSet<>();

    @Transient
    private Set<Image> images = new LinkedHashSet<>();

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Policy> policies = new LinkedHashSet<>();

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new LinkedHashSet<>();

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Room> rooms = new LinkedHashSet<>();

    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Inventory> inventories = new LinkedHashSet<>();

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
        if (name != null && (slug == null || slug.isBlank())) {
            this.slug = name.trim().toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("(^-|-$)", "");
        }
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating == null ? null : BigDecimal.valueOf(starRating);
    }

    public Integer getStarRating() {
        return starRating == null ? null : starRating.intValue();
    }

    public Boolean getIsActive() {
        return "ACTIVE".equals(status);
    }

    public void setIsActive(Boolean active) {
        this.status = Boolean.TRUE.equals(active) ? "ACTIVE" : "DRAFT";
    }

    public String getContactName() {
        return null;
    }

    public void setContactName(String contactName) {
        // No matching column in hotels; retained for legacy mapper compatibility.
    }

    public String getContactPhone() {
        return phone;
    }

    public void setContactPhone(String contactPhone) {
        this.phone = contactPhone;
    }
}
