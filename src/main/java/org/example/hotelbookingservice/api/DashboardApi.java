package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.response.dashboard.DashboardStatsResponse;
import org.example.hotelbookingservice.dto.response.dashboard.LatestReviewResponse;
import org.example.hotelbookingservice.dto.response.dashboard.NewestBookingResponse;
import org.example.hotelbookingservice.dto.response.dashboard.RevenuePointResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1")
@Tag(name = "Dashboard", description = "Dashboard statistics, revenue charts, latest reviews, and newest bookings")
public interface DashboardApi {

    @Operation(summary = "Get dashboard stats")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
    @GetMapping("/dashboard/stats")
    ApiResponse<DashboardStatsResponse> dashboardStats(@RequestParam(required = false) UUID hotelId, Authentication authentication);

    @Operation(summary = "Get dashboard revenue chart")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
    @GetMapping("/dashboard/revenue-chart")
    ApiResponse<List<RevenuePointResponse>> revenueChart(
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    );

    @Operation(summary = "Get latest dashboard reviews")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
    @GetMapping("/dashboard/latest-reviews")
    ApiResponse<List<LatestReviewResponse>> latestReviews(@RequestParam(required = false) UUID hotelId, Authentication authentication);

    @Operation(summary = "Get newest dashboard bookings")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER') or hasAuthority('OWNER')")
    @GetMapping("/dashboard/newest-bookings")
    ApiResponse<List<NewestBookingResponse>> newestBookings(@RequestParam(required = false) UUID hotelId, Authentication authentication);

    @Operation(summary = "Get commission revenue chart")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping({"/admin/commission-packages/revenue/chart", "/commission-revenue"})
    ApiResponse<Object> commissionRevenue(
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    );
}
