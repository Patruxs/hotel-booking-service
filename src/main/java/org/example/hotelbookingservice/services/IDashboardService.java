package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.response.dashboard.DashboardStatsResponse;
import org.example.hotelbookingservice.dto.response.dashboard.LatestReviewResponse;
import org.example.hotelbookingservice.dto.response.dashboard.NewestBookingResponse;
import org.example.hotelbookingservice.dto.response.dashboard.RevenuePointResponse;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IDashboardService {
    DashboardStatsResponse dashboardStats(UUID hotelId, Authentication authentication);

    List<RevenuePointResponse> revenueChart(UUID hotelId, String groupBy, Integer year, LocalDate from, LocalDate to, Authentication authentication);

    List<LatestReviewResponse> latestReviews(UUID hotelId, Authentication authentication);

    List<NewestBookingResponse> newestBookings(UUID hotelId, Authentication authentication);

    Object commissionRevenue(UUID hotelId, Integer year, LocalDate from, LocalDate to, Authentication authentication);
}
