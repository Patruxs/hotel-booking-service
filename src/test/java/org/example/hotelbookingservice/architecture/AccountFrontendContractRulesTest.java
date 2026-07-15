package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AccountFrontendContractRulesTest {
    private static final Path USER_API = Path.of("frontend/src/features/user/api.ts");
    private static final Path BOOKINGS_API = Path.of("frontend/src/features/bookings/api.ts");
    private static final Path REVIEWS_API = Path.of("frontend/src/features/reviews/api.ts");
    private static final Path PROFILE_PAGE = Path.of("frontend/src/pages/kinyias/ProfilePage.tsx");
    private static final Path MY_BOOKINGS_PAGE = Path.of("frontend/src/pages/kinyias/MyBookingsPage.tsx");
    private static final Path MY_BOOKING_DETAIL_PAGE = Path.of("frontend/src/pages/kinyias/MyBookingDetailPage.tsx");
    private static final Path MY_REVIEWS_PAGE = Path.of("frontend/src/pages/kinyias/MyReviewsPage.tsx");
    private static final Path CURRENT_USER_RESPONSE = Path.of("src/main/java/org/example/hotelbookingservice/dto/response/auth/AuthResponses.java");
    private static final Path REVIEW_RESPONSE = Path.of("src/main/java/org/example/hotelbookingservice/dto/response/review/ReviewResponse.java");
    private static final Path USER_CONTROLLER = Path.of("src/main/java/org/example/hotelbookingservice/controller/UserController.java");
    private static final Path MEDIA_API = Path.of("src/main/java/org/example/hotelbookingservice/api/MediaApi.java");
    private static final Path REVIEW_API = Path.of("src/main/java/org/example/hotelbookingservice/api/ReviewOperationsApi.java");

    @Test
    void profileAdapterUsesLiveSpringAccountContracts() {
        String source = content(USER_API);

        assertThat(source)
                .contains("api.get(\"/users/me\")")
                .contains("api.patch(\"/users/me\"")
                .contains("api.post(\"/users/me/change-password\"")
                .contains("api.post(\"/uploads/avatar\"")
                .contains("api.delete(\"/uploads/avatar\")");
        assertThat(source)
                .as("Avatar upload/delete must have live Spring contracts, not mock-only behavior")
                .doesNotContain("uploadAvatar: (_formData: FormData) => mockOnly");
    }

    @Test
    void customerBookingRoutesUseOwnedLiveSpringContractsAndHideDisabledPaymentActions() {
        String source = content(BOOKINGS_API);

        assertThat(source)
                .contains("api.get(\"/bookings/me\"")
                .contains("api.get(`/bookings/me/${bookingId}`")
                .contains("api.patch(`/bookings/me/${bookingId}/cancel`")
                .contains("api.post(`/bookings/${bookingId}/payments/vnpay`, {})");
        assertThat(content(MY_BOOKING_DETAIL_PAGE))
                .contains("VNPAY_ENABLED")
                .contains("Repay Online (VNPAY)")
                .contains("Pay Online (VNPAY)");
    }

    @Test
    void customerReviewsUseMineContractsAndSpringDtoMapping() {
        String source = content(REVIEWS_API);

        assertThat(source)
                .contains("api.get(\"/reviews/mine\"")
                .contains("api.patch(`/reviews/${id}/mine`")
                .contains("comment: body?.content ?? body?.comment ?? body?.title")
                .contains("isHidden: raw.isHidden ?? raw.visible === false");
        assertThat(source)
                .as("Route inventory and live adapter must not target the stale users/me reviews path")
                .doesNotContain("/users/me/reviews");
    }

    @Test
    void accountPagesDoNotImportFixturesOrMockHelpersDirectly() {
        for (Path page : new Path[]{PROFILE_PAGE, MY_BOOKINGS_PAGE, MY_BOOKING_DETAIL_PAGE, MY_REVIEWS_PAGE}) {
            assertThat(content(page))
                    .as(page + " should consume hooks/adapters only")
                    .doesNotContain("@/mocks/")
                    .doesNotContain("mockApi")
                    .doesNotContain("mockOnly")
                    .doesNotContain("mockOrRequest");
        }
    }

    @Test
    void springAccountContractsExposeFieldsAndRoutesUsedByAccountPages() {
        assertThat(content(CURRENT_USER_RESPONSE))
                .contains("String phone")
                .contains("LocalDate dob")
                .contains("LocalDate dateOfBirth");
        assertThat(content(REVIEW_RESPONSE))
                .contains("HotelSummary hotel");
        assertThat(content(USER_CONTROLLER))
                .contains("@GetMapping(\"/me\")")
                .contains("@PatchMapping(\"/me\")")
                .contains("@PostMapping(\"/me/change-password\")");
        assertThat(content(MEDIA_API))
                .contains("@PostMapping(value = \"/uploads/avatar\"")
                .contains("@DeleteMapping(\"/uploads/avatar\")");
        assertThat(content(REVIEW_API))
                .contains("@GetMapping(\"/reviews/mine\")")
                .contains("@PatchMapping(\"/reviews/{reviewId}/mine\")");
    }

    private String content(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
