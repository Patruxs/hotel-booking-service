package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.hotelbookingservice.enums.RoomType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "room_types")
public class Room {
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

    @NotNull
    @Column(name = "max_guests", nullable = false)
    private Integer capacity;

    @Column(name = "number_of_bedrooms")
    private Integer numberOfBedrooms;

    @NotNull
    @Column(name = "price_per_night", nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(name = "description")
    private String description;

    @Transient
    @Enumerated(EnumType.STRING)
    private RoomType type;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Transient
    private Integer amount;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookingroom> bookingrooms = new LinkedHashSet<>();

    @Transient
    private Set<Image> images = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Roomamenity> roomAmenities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PhysicalRoom> physicalRooms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL, orphanRemoval = true)
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
}
