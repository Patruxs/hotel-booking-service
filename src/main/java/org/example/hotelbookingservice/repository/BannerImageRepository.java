package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.BannerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BannerImageRepository extends JpaRepository<BannerImage, UUID> {
    List<BannerImage> findByBanner_IdOrderBySortOrderAsc(UUID bannerId);

    void deleteByBanner_Id(UUID bannerId);
}
