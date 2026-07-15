package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "hotel_commission_packages")
public class HotelCommissionPackage {
    @Id
    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @NotNull
    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commission_package_id", nullable = false)
    private CommissionPackage commissionPackage;

    @NotNull
    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    public UUID getCommissionPackageId() {
        return commissionPackage == null ? null : commissionPackage.getId();
    }
}
