package org.example.hotelbookingservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.api.DashboardApi;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.response.dashboard.DashboardStatsResponse;
import org.example.hotelbookingservice.dto.response.dashboard.LatestReviewResponse;
import org.example.hotelbookingservice.dto.response.dashboard.NewestBookingResponse;
import org.example.hotelbookingservice.dto.response.dashboard.RevenuePointResponse;
import org.example.hotelbookingservice.services.IDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {
    private final IDashboardService dashboardService;

    @Override
    public ApiResponse<DashboardStatsResponse> dashboardStats(@RequestParam(required = false) UUID hotelId, Authentication authentication) {
        return ApiResponse.<DashboardStatsResponse>builder().status(200).message("Success").data(dashboardService.dashboardStats(hotelId, authentication)).build();
    }

    @Override
    public ApiResponse<List<RevenuePointResponse>> revenueChart(
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        return ApiResponse.<List<RevenuePointResponse>>builder().status(200).message("Success").data(dashboardService.revenueChart(hotelId, groupBy, year, from, to, authentication)).build();
    }

    @Override
    public ApiResponse<List<LatestReviewResponse>> latestReviews(@RequestParam(required = false) UUID hotelId, Authentication authentication) {
        return ApiResponse.<List<LatestReviewResponse>>builder().status(200).message("Success").data(dashboardService.latestReviews(hotelId, authentication)).build();
    }

    @Override
    public ApiResponse<List<NewestBookingResponse>> newestBookings(@RequestParam(required = false) UUID hotelId, Authentication authentication) {
        return ApiResponse.<List<NewestBookingResponse>>builder().status(200).message("Success").data(dashboardService.newestBookings(hotelId, authentication)).build();
    }

    @Override
    public ApiResponse<Object> commissionRevenue(
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        return ApiResponse.<Object>builder().status(200).message("Success").data(dashboardService.commissionRevenue(hotelId, year, from, to, authentication)).build();
    }
}
