package org.example.hotelbookingservice.dto.response.dashboard;

import java.math.BigDecimal;

public record DashboardStatsResponse(
        long totalUsers,
        long totalBookings,
        BigDecimal revenue,
        long activeHotels
) {
}
