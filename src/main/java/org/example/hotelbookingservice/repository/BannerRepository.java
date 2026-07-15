package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {
    List<Banner> findAllByOrderByPositionAsc();

    @Query("""
            select banner
            from Banner banner
            where banner.active = true
              and (banner.startsAt is null or banner.startsAt <= :now)
              and (banner.endsAt is null or banner.endsAt >= :now)
            order by banner.position asc
            """)
    List<Banner> findActiveForPublic(@Param("now") Instant now);

    @Query("select coalesce(max(banner.position), 0) + 1 from Banner banner")
    int nextPosition();
}
