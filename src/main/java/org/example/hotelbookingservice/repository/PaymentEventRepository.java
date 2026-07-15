package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {
    @Query("select event from PaymentEvent event where event.payment.id = :paymentId order by event.createdAt")
    List<PaymentEvent> findByPaymentIdOrderByCreatedAt(@Param("paymentId") UUID paymentId);

    @Modifying
    @Query(value = """
            insert into payment_events (id, payment_id, event_type, payload)
            values (:id, :paymentId, :eventType, cast(:payload as jsonb))
            """, nativeQuery = true)
    void insertEvent(@Param("id") UUID id,
                     @Param("paymentId") UUID paymentId,
                     @Param("eventType") String eventType,
                     @Param("payload") String payload);
}
