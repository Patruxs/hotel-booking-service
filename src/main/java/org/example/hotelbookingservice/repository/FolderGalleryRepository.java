package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.FolderGallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FolderGalleryRepository extends JpaRepository<FolderGallery, UUID> {
    List<FolderGallery> findByOwnerAccountIdOrderByFolderNameAsc(UUID ownerAccountId);

    Optional<FolderGallery> findByOwnerAccountIdAndFolderName(UUID ownerAccountId, String folderName);

    boolean existsByIdAndOwnerAccountId(UUID id, UUID ownerAccountId);
}
