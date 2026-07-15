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
public class HotelamenityId implements Serializable {
    private static final long serialVersionUID = -3944573229208150351L;
    @NotNull
    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @NotNull
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    public UUID getAmenityId() {
        return accountId;
    }

    public void setAmenityId(UUID amenityId) {
        this.accountId = amenityId;
    }

    public void setAmenityId(Integer amenityId) {
        this.accountId = amenityId == null ? null : new UUID(0L, amenityId.longValue());
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId == null ? null : new UUID(0L, hotelId.longValue());
    }

    public void setHotelId(UUID hotelId) {
        this.hotelId = hotelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HotelamenityId entity = (HotelamenityId) o;
        return Objects.equals(this.accountId, entity.accountId) &&
                Objects.equals(this.hotelId, entity.hotelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, hotelId);
    }

}
