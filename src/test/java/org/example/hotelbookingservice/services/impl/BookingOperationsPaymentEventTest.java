package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.FrontendProperties;
import org.example.hotelbookingservice.config.VnpayProperties;
import org.example.hotelbookingservice.repository.operations.BookingOperationsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingOperationsPaymentEventTest {
    @Mock BookingOperationsRepository bookingOperationsRepository;
    @Mock PlatformTransactionManager transactionManager;
    @Mock VnpayProperties vnpayProperties;
    @Mock FrontendProperties frontendProperties;

    @Test
    void recordPaymentEvent_serializesPayloadThroughRepositoryBoundary() {
        BookingOperationsServiceImpl service = new BookingOperationsServiceImpl(
                bookingOperationsRepository,
                Clock.systemUTC(),
                transactionManager,
                vnpayProperties,
                frontendProperties
        );
        UUID paymentId = UUID.fromString("71000000-0000-4000-8000-000000000001");
        ArgumentCaptor<UUID> eventId = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);

        ReflectionTestUtils.invokeMethod(service, "recordPaymentEvent", paymentId, "VNPAY_RETURN", Map.of("vnp_ResponseCode", "00"));

        verify(bookingOperationsRepository).insertPaymentEvent(eventId.capture(), eq(paymentId), eq("VNPAY_RETURN"), payload.capture());
        assertThat(eventId.getValue()).isNotNull();
        assertThat(payload.getValue()).contains("\"vnp_ResponseCode\":\"00\"");
    }
}
