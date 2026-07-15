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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Size(max = 64)
    @NotNull
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Size(max = 160)
    @NotNull
    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Size(max = 16)
    @NotNull
    @Column(name = "discount_type", nullable = false, length = 16)
    private String discountType;

    @NotNull
    @Column(name = "discount_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount", precision = 14, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_booking_amount", precision = 14, scale = 2)
    private BigDecimal minBookingAmount;

    @Column(name = "total_usage_limit")
    private Integer totalUsageLimit;

    @Column(name = "per_user_usage_limit")
    private Integer perUserUsageLimit;

    @NotNull
    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHotelId() {
        return hotel == null ? null : hotel.getUuid();
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }
}
