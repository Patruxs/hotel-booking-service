package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "hotel_members")
public class Hotelamenity {
    @EmbeddedId
    private HotelamenityId id;

    @MapsId("hotelId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @MapsId("accountId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private User account;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Transient
    public Amenity getAmenity() {
        return null;
    }

    public void setAmenity(Amenity amenity) {
        // Compatibility shim for legacy services. Slice 5 removes hotel amenity JPA usage.
    }
}
