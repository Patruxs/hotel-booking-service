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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hotel_policies")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @NotNull
    @Column(name = "type", nullable = false, length = 64)
    private String type = "GENERAL";

    @NotNull
    @Column(name = "title", nullable = false, length = 200)
    private String title = "Policy";

    @NotNull
    @Column(name = "content", nullable = false)
    private String content = "";

    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Transient
    private Integer limitedAge;
    @Transient
    private Integer minAdultAge;
    @Transient
    private Boolean childrenAllowed;
    @Transient
    private Boolean animalAllowed;
    @Transient
    private LocalDate freeCancellation;
    @Transient
    private LocalDate starCheckinTime;
    @Transient
    private LocalDate endCheckinTime;
    @Transient
    private LocalDate starCheckoutTime;
    @Transient
    private LocalDate endCheckoutTime;
    @Transient
    private Boolean noAdvancePaid;

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
