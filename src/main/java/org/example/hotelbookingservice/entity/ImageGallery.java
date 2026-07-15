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
@Table(name = "gallery_images")
public class ImageGallery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "folder_id", nullable = false)
    private FolderGallery folder;

    @Column(name = "folder_id", insertable = false, updatable = false)
    private UUID folderId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_asset_id", nullable = false)
    private Image imageAsset;

    @Column(name = "image_asset_id", insertable = false, updatable = false)
    private UUID imageAssetId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getFolderId() {
        return folderId == null && folder != null ? folder.getId() : folderId;
    }

    public UUID getImageAssetId() {
        return imageAssetId == null && imageAsset != null ? imageAsset.getUuid() : imageAssetId;
    }
}
