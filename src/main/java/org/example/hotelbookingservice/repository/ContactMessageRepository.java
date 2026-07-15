package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {
    @EntityGraph(attributePaths = "handledBy")
    @Query("""
            select contact
            from ContactMessage contact
            where (:status is null or contact.status = :status)
              and (:query is null or lower(concat(contact.name, ' ', coalesce(contact.email, ''), ' ', coalesce(contact.subject, ''))) like :query)
            order by contact.createdAt desc
            """)
    Page<ContactMessage> findForAdmin(@Param("status") String status, @Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = "handledBy")
    @Query("select contact from ContactMessage contact where contact.id = :id")
    Optional<ContactMessage> findDetailById(@Param("id") UUID id);
}
