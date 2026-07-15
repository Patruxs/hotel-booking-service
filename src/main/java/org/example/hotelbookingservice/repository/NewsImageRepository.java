package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.NewsImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NewsImageRepository extends JpaRepository<NewsImage, UUID> {
    List<NewsImage> findByNews_IdOrderBySortOrderAsc(UUID newsId);

    List<NewsImage> findByNews_IdAndIdIn(UUID newsId, Collection<UUID> ids);

    void deleteByNews_Id(UUID newsId);
}
