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
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "booking_guests")
public class GuestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @NotNull
    @Size(max = 120)
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Size(max = 40)
    @Column(name = "identity_number", length = 40)
    private String identityNumber;

    @Size(max = 32)
    @Column(name = "phone", length = 32)
    private String phone;

    @NotNull
    @Column(name = "is_primary", nullable = false)
    private Boolean primaryGuest = false;

    @NotNull
    @Column(name = "guest_order", nullable = false)
    private Integer guestOrder = 0;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckIn checkIn;

    @Transient
    private Booking booking;

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
