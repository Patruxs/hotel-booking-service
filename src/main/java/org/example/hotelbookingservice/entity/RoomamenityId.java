package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class RoomamenityId implements Serializable {
    private static final long serialVersionUID = 6226235298449020114L;
    @NotNull
    @Column(name = "amenity_id", nullable = false)
    private UUID amenityId;

    @NotNull
    @Column(name = "room_type_id", nullable = false)
    private UUID roomId;

    public void setAmenityId(Integer amenityId) {
        this.amenityId = amenityId == null ? null : new UUID(0L, amenityId.longValue());
    }

    public void setAmenityId(UUID amenityId) {
        this.amenityId = amenityId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId == null ? null : new UUID(0L, roomId.longValue());
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RoomamenityId entity = (RoomamenityId) o;
        return Objects.equals(this.amenityId, entity.amenityId) &&
                Objects.equals(this.roomId, entity.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amenityId, roomId);
    }

}
