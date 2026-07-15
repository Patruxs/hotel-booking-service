package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipient_IdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipient_Id(UUID id, UUID recipientId);

    long countByRecipient_IdAndReadAtIsNull(UUID recipientId);

    @Modifying
    @Query("""
            update Notification notification
            set notification.readAt = :now
            where notification.recipient.id = :recipientId
              and notification.readAt is null
            """)
    int markAllRead(@Param("recipientId") UUID recipientId, @Param("now") Instant now);

    int deleteByIdAndRecipient_Id(UUID id, UUID recipientId);
}
