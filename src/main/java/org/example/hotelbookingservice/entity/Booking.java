package org.example.hotelbookingservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.example.hotelbookingservice.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Size(max = 80)
    @NotNull
    @Column(name = "booking_reference", nullable = false, length = 80)
    private String bookingReference;

    @NotNull
    @Column(name = "check_in", nullable = false)
    private LocalDate checkinDate;

    @NotNull
    @Column(name = "check_out", nullable = false)
    private LocalDate checkoutDate;

    @Size(max = 120)
    @NotNull
    @Column(name = "guest_name", nullable = false, length = 120)
    private String customerName;

    @Size(max = 320)
    @NotNull
    @Column(name = "guest_email", nullable = false, length = 320)
    private String guestEmail;

    @Size(max = 32)
    @NotNull
    @Column(name = "guest_phone", nullable = false, length = 32)
    private String guestPhone;

    @Column(name = "note", length = 1000)
    private String specialRequire;

    @NotNull
    @Column(name = "status", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @NotNull
    @Column(name = "subtotal_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Size(max = 64)
    @Column(name = "commission_package_code", length = 64)
    private String commissionPackageCode;

    @NotNull
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @NotNull
    @Column(name = "commission_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(name = "pending_expires_at")
    private Instant pendingExpiresAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "no_show_at")
    private Instant noShowAt;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createAt = Instant.now();

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "promotion_id")
    private UUID promotionId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookingroom> bookingrooms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new LinkedHashSet<>();

    @Transient
    private Set<GuestDetail> guestDetails = new LinkedHashSet<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private CheckIn checkIn;

    @Transient
    private Integer adultAmount;

    @Transient
    private Integer childrenAmount;

    @Transient
    private String roomNumber;

    @Transient
    private String cancelReason;

    @Transient
    private Float refund;

    @Transient
    private Float damageFee;

    @Transient
    private String damageDescription;

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

    public LocalDate getCreateAt() {
        return createAt == null ? null : createAt.atZone(ZoneOffset.UTC).toLocalDate();
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt == null ? null : createAt.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public Float getTotalPrice() {
        return totalPrice == null ? null : totalPrice.floatValue();
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice == null ? null : BigDecimal.valueOf(totalPrice);
        this.subtotalAmount = this.totalPrice == null ? BigDecimal.ZERO : this.totalPrice;
    }
}
