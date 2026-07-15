package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Size(max = 32)
    @NotNull
    @Column(name = "status", nullable = false, length = 32)
    private String paymentStatus;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createAt = Instant.now();

    @Size(max = 32)
    @NotNull
    @Column(name = "provider", nullable = false, length = 32)
    private String paymentMethod = "VNPAY";

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Size(max = 160)
    @NotNull
    @Column(name = "merchant_txn_ref", nullable = false, length = 160)
    private String code;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidPrice = BigDecimal.ZERO;

    @Size(max = 8)
    @NotNull
    @Column(name = "currency", nullable = false, length = 8)
    private String currency = "VND";

    @Column(name = "payment_url")
    private String paymentUrl;

    @Size(max = 160)
    @Column(name = "provider_transaction_no", length = 160)
    private String providerTransactionNo;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaymentEvent> events = new LinkedHashSet<>();

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

    public Float getPaidPrice() {
        return paidPrice == null ? null : paidPrice.floatValue();
    }

    public void setPaidPrice(Float paidPrice) {
        this.paidPrice = paidPrice == null ? null : BigDecimal.valueOf(paidPrice);
    }
}
