package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class RoomamenityId implements Serializable {
    private static final long serialVersionUID = 6226235298449020114L;
    @NotNull
    @Column(name = "amenity_id", nullable = false)
    private Integer amenityId;

    @NotNull
    @Column(name = "room_id", nullable = false)
    private Integer roomId;

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