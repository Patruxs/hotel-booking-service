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

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "news_images")
public class NewsImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(name = "news_id", insertable = false, updatable = false)
    private UUID newsId;

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

    public UUID getNewsId() {
        return newsId == null && news != null ? news.getId() : newsId;
    }

    public UUID getImageAssetId() {
        return imageAssetId == null && imageAsset != null ? imageAsset.getUuid() : imageAssetId;
    }
}
