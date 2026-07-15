package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdminFrontendContractRulesTest {
    private static final Path HOTELS_API = Path.of("frontend/src/features/hotels/api.ts");
    private static final Path ROOM_TYPES_API = Path.of("frontend/src/features/room-types/api.ts");
    private static final Path ROOMS_API = Path.of("frontend/src/features/rooms/api.ts");
    private static final Path INVENTORY_API = Path.of("frontend/src/features/inventory/api.ts");
    private static final Path USER_API = Path.of("frontend/src/features/user/api.ts");
    private static final Path NOTIFICATIONS_API = Path.of("frontend/src/features/notifications/api.ts");
    private static final Path COMMISSIONS_API = Path.of("frontend/src/features/commissions/api.ts");
    private static final Path USER_CONTROLLER = Path.of("src/main/java/org/example/hotelbookingservice/controller/UserController.java");
    private static final Path HOTEL_OPERATIONS_CONTROLLER = Path.of("src/main/java/org/example/hotelbookingservice/controller/HotelOperationsController.java");
    private static final Path CONTENT_CONTROLLER = Path.of("src/main/java/org/example/hotelbookingservice/controller/ContentController.java");

    @Test
    void reachableAdminManagementAdaptersDoNotUseMockOnlyContracts() {
        for (Path adapter : new Path[]{HOTELS_API, ROOM_TYPES_API, ROOMS_API, INVENTORY_API, USER_API, NOTIFICATIONS_API, COMMISSIONS_API}) {
            assertThat(content(adapter))
                    .as(adapter + " must call Spring in live mode")
                    .doesNotContain("mockOnly");
        }
    }

    @Test
    void adminHotelRoomAndInventoryAdaptersUseSpringOperationsEndpoints() {
        assertThat(content(HOTELS_API))
                .contains("api.post(\"/hotels\"")
                .contains("api.patch(`/hotels/${id}`")
                .contains("api.delete(`/hotels/${id}`")
                .contains("api.get(`/hotels/${hotelId}/members`")
                .contains("api.post(`/hotels/${hotelId}/members`")
                .contains("api.delete(`/hotels/${hotelId}/members/${userId}`");
        assertThat(content(ROOM_TYPES_API))
                .contains("api.post(`/hotels/${hotelId}/room-types`")
                .contains("api.patch(`/hotels/${_hotelId}/room-types/${id}`")
                .contains("api.delete(`/hotels/${_hotelId}/room-types/${_id}`");
        assertThat(content(ROOMS_API))
                .contains("api.post(`/hotels/${_hotelId}/rooms`")
                .contains("api.patch(`/hotels/${_hotelId}/rooms/${roomId}`")
                .contains("api.delete(`/hotels/${_hotelId}/rooms/${roomId}`");
        assertThat(content(INVENTORY_API))
                .contains("api.get(`/hotels/${hotelId}/room-types/${roomTypeId}/inventory`")
                .contains("api.put(")
                .contains("`/hotels/${hotelId}/room-types/${roomTypeId}/inventory`")
                .contains("toInventoryRequest");
    }

    @Test
    void adminUserNotificationAndCommissionAdaptersUseSpringContracts() {
        assertThat(content(USER_API))
                .contains("api.get(\"/users\"")
                .contains("api.patch(`/users/${_id}`")
                .contains("api.post(\"/roles/assign-to-user\"");
        assertThat(content(NOTIFICATIONS_API))
                .contains("api.get(\"/notifications\"")
                .contains("api.get(\"/notifications/unread-count\"")
                .contains("api.patch(`/notifications/${id}/read`")
                .contains("api.patch(\"/notifications/read-all\"")
                .contains("api.delete(`/notifications/${id}`");
        assertThat(content(COMMISSIONS_API))
                .contains("api.get(\"/admin/commission-packages\"")
                .contains("api.post(\"/admin/commission-packages\"")
                .contains("api.patch(`/admin/commission-packages/${id}`")
                .contains("api.put(`/hotels/${hotelId}/commission-package/${commissionPackageId}`");
    }

    @Test
    void backendExposesContractsNeededByReachableAdminFeatures() {
        assertThat(content(HOTEL_OPERATIONS_CONTROLLER))
                .contains("@GetMapping(\"/hotels/{hotelId}/members\")")
                .contains("@PostMapping(\"/hotels/{hotelId}/members\")")
                .contains("@DeleteMapping(\"/hotels/{hotelId}/members/{accountId}\")")
                .contains("@PutMapping(\"/hotels/{hotelId}/room-types/{roomTypeId}/inventory\")");
        assertThat(content(CONTENT_CONTROLLER))
                .contains("public ApiResponse<ListResponse<NotificationResponse>> notifications")
                .contains("public ApiResponse<CommissionPackageResponse> updateCommissionPackage");
        assertThat(content(USER_CONTROLLER))
                .contains("public ApiResponse<UserListItem> updateUser");
    }

    private String content(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
