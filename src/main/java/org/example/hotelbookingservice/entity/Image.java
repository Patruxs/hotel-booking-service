package org.example.hotelbookingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "image_assets")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @NotNull
    @Column(name = "url", nullable = false)
    private String path;

    @NotNull
    @Column(name = "provider", nullable = false, length = 32)
    private String provider = "CLOUDINARY";

    @Column(name = "owner_account_id")
    private UUID ownerAccountId;

    @Column(name = "public_id")
    private String publicId;

    @Column(name = "secure_url")
    private String secureUrl;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "bytes")
    private Long bytes;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Transient
    private Room room;

    @Transient
    private Hotel hotel;

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
