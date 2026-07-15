package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PublicBookingFrontendAdapterRulesTest {
    private static final Path BOOKINGS_API = Path.of("frontend/src/features/bookings/api.ts");
    private static final Path HOTELS_API = Path.of("frontend/src/features/hotels/api.ts");
    private static final Path ROOM_TYPES_API = Path.of("frontend/src/features/room-types/api.ts");
    private static final Path NEWS_API = Path.of("frontend/src/features/news/api.ts");
    private static final Path HOTEL_DETAIL_PAGE = Path.of("frontend/src/pages/kinyias/HotelDetailPage.tsx");
    private static final Path BOOKING_PAYMENT_PAGE = Path.of("frontend/src/features/bookings/components/BookingPayment.tsx");

    @Test
    void bookingAdapterDoesNotSendClientTotalAmountToSpring() {
        String source = content(BOOKINGS_API);
        String mapperBody = source.substring(
                source.indexOf("export const toSpringBookingCreateRequest"),
                source.indexOf("export const bookingsApi")
        );

        assertThat(mapperBody).contains("checkIn", "checkOut", "guestName", "guestEmail", "guestPhone", "promotionCode", "items");
        assertThat(mapperBody)
                .as("Spring owns final booking totals; frontend booking adapter must not submit totalAmount")
                .doesNotContain("totalAmount");
        assertThat(content(HOTEL_DETAIL_PAGE))
                .as("Hotel detail should hand off booking inputs only, not a client total")
                .doesNotContain("total_price");
    }

    @Test
    void publicAdaptersUseCanonicalSpringEndpointsAndGatePaymentInitiation() {
        assertThat(content(HOTELS_API)).contains("api.get(\"/hotels\"");
        assertThat(content(ROOM_TYPES_API))
                .contains("api.get(`/hotels/${hotelId}/room-types`")
                .contains("api.get(`/hotels/${hotelId}/room-types/available`");
        assertThat(content(BOOKINGS_API))
                .contains("api.post(`/hotels/${hotelId}/bookings`")
                .contains("api.post(`/bookings/${bookingId}/payments/vnpay`, {})");
        assertThat(content(BOOKING_PAYMENT_PAGE))
                .contains("VNPAY_ENABLED")
                .contains("if (!VNPAY_ENABLED)")
                .contains("router.push(`/me/my-bookings/${bookingId}`)");
    }

    @Test
    void liveNewsMappingDoesNotFallBackToFixturesForUnexpectedSpringShape() {
        String source = content(NEWS_API);
        String normalizeList = source.substring(
                source.indexOf("function normalizeList"),
                source.indexOf("export const newsApi")
        );

        assertThat(normalizeList)
                .as("Unexpected live Spring payloads must fail visibly instead of showing mock news")
                .doesNotContain("return listMockNews()");
        assertThat(normalizeList).contains("Unexpected news list response from Spring API");
    }

    private String content(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
