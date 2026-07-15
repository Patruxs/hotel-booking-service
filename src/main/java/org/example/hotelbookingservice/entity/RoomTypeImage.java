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
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "room_type_images")
public class RoomTypeImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private Room roomType;

    @Column(name = "room_type_id", insertable = false, updatable = false)
    private UUID roomTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_asset_id")
    private Image imageAsset;

    @Column(name = "image_asset_id", insertable = false, updatable = false)
    private UUID imageAssetId;

    @NotNull
    @Column(name = "url", nullable = false)
    private String url;

    @NotNull
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getRoomTypeId() {
        return roomTypeId == null && roomType != null ? roomType.getUuid() : roomTypeId;
    }

    public UUID getImageAssetId() {
        return imageAssetId == null && imageAsset != null ? imageAsset.getUuid() : imageAssetId;
    }
}
