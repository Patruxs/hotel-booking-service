package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.ImageGallery;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ImageGalleryRepository extends JpaRepository<ImageGallery, UUID> {
    @EntityGraph(attributePaths = "imageAsset")
    @Query("""
            select imageGallery
            from ImageGallery imageGallery
            where imageGallery.folder.id = :folderId
            order by imageGallery.createdAt desc
            """)
    List<ImageGallery> findByFolderIdWithImageAssetOrderByCreatedAtDesc(@Param("folderId") UUID folderId);

    @Query("""
            select count(imageGallery) > 0
            from ImageGallery imageGallery
            where imageGallery.folder.id = :folderId
              and imageGallery.imageAsset.id = :imageAssetId
            """)
    boolean existsByFolderIdAndImageAssetId(@Param("folderId") UUID folderId, @Param("imageAssetId") UUID imageAssetId);
}
