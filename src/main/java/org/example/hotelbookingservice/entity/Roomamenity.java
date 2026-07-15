package org.example.hotelbookingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "room_type_amenities")
public class Roomamenity {
    @EmbeddedId
    private RoomamenityId id;

    @MapsId("amenityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private Room room;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
