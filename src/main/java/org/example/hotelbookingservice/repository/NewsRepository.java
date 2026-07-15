package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NewsRepository extends JpaRepository<News, UUID> {
    boolean existsBySlug(String slug);

    Optional<News> findBySlugAndStatusAndPublishedAtLessThanEqual(String slug, String status, Instant publishedAt);

    @Query("""
            select news
            from News news
            where (cast(:status as text) is null or news.status = :status)
              and (cast(:query as text) is null or lower(concat(news.title, ' ', coalesce(news.summary, ''), ' ', coalesce(news.content, ''))) like :query)
            order by coalesce(news.publishedAt, news.createdAt) desc
            """)
    Page<News> findForAdmin(@Param("status") String status, @Param("query") String query, Pageable pageable);

    @Query("""
            select news
            from News news
            where news.status = 'PUBLISHED'
              and news.publishedAt <= :now
              and (cast(:query as text) is null or lower(concat(news.title, ' ', coalesce(news.summary, ''), ' ', coalesce(news.content, ''))) like :query)
            order by coalesce(news.publishedAt, news.createdAt) desc
            """)
    Page<News> findForPublic(@Param("query") String query, @Param("now") Instant now, Pageable pageable);
}
