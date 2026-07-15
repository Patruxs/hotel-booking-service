package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, UUID> {
    List<ReviewImage> findByReview_IdOrderBySortOrderAsc(UUID reviewId);
}
