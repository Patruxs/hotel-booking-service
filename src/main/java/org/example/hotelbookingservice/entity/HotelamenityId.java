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
public class HotelamenityId implements Serializable {
    private static final long serialVersionUID = -3944573229208150351L;
    @NotNull
    @Column(name = "hotel_id", nullable = false)
    private Integer hotelId;

    @NotNull
    @Column(name = "amenity_id", nullable = false)
    private Integer amenityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HotelamenityId entity = (HotelamenityId) o;
        return Objects.equals(this.amenityId, entity.amenityId) &&
                Objects.equals(this.hotelId, entity.hotelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amenityId, hotelId);
    }

}