package org.example.hotelbookingservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "check_ins")
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "booking_id", insertable = false, updatable = false)
    private UUID bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_in_by_account_id")
    private User checkedInBy;

    @Column(name = "checked_in_by_account_id", insertable = false, updatable = false)
    private UUID checkedInByAccountId;

    @NotNull
    @Column(name = "checked_in_at", nullable = false)
    private Instant checkedInAt = Instant.now();

    @Column(name = "checked_out_at")
    private Instant checkedOutAt;

    @Size(max = 1000)
    @Column(name = "note", length = 1000)
    private String note;

    @OneToMany(mappedBy = "checkIn", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GuestDetail> guestDetails = new LinkedHashSet<>();
}
