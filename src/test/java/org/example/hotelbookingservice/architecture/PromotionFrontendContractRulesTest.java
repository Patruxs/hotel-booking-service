package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionFrontendContractRulesTest {
    private static final Path PROMOTION_API = Path.of("frontend/src/features/promotion/api.ts");
    private static final Path BOOKING_PAYMENT = Path.of("frontend/src/features/bookings/components/BookingPayment.tsx");
    private static final Path BOOKING_SERVICE = Path.of("src/main/java/org/example/hotelbookingservice/services/impl/BookingOperationsServiceImpl.java");

    @Test
    void promotionAdapterUsesLiveSpringEndpointsInLiveMode() {
        String source = content(PROMOTION_API);

        assertThat(source)
                .contains("api.get(\"/admin/promotions\"")
                .contains("api.get(\"/promotions/public\"")
                .contains("api.get(`/admin/promotions/${id}`")
                .contains("api.post(\"/admin/promotions\"")
                .contains("api.patch(`/admin/promotions/${id}`")
                .contains("api.delete(`/admin/promotions/${id}`");
        assertThat(source)
                .as("Reachable promotion adapter must not be mock-only in live mode")
                .doesNotContain("mockOnly");
    }

    @Test
    void checkoutQueriesPublicPromotionPreviewButKeepsSpringBookingAuthoritative() {
        String source = content(BOOKING_PAYMENT);

        assertThat(source)
                .contains("usePublicPromotionsQuery")
                .contains("Spring will calculate the final total when booking is created")
                .contains("Promotion code will be validated by Spring when you confirm booking");
        assertThat(source)
                .as("Live checkout should query public promotion lookup/search, not disable it outside mock mode")
                .doesNotContain("}, useMocks)");
    }

    @Test
    void bookingServiceRemainsFinalPromotionAuthority() {
        String source = content(BOOKING_SERVICE);

        assertThat(source)
                .contains("findActivePromotionForBooking")
                .contains("countPromotionUsesByAccount")
                .contains("Promotion usage limit has been reached")
                .contains("Promotion code is invalid");
    }

    private String content(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
