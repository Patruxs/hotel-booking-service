package org.example.hotelbookingservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.response.RevenueStatisticResponse;
import org.example.hotelbookingservice.services.IRevenueService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import org.example.hotelbookingservice.api.RevenueApi;

@RestController
@RequestMapping("/api/v1/revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RevenueController implements RevenueApi {

    IRevenueService revenueService;

    @Override
    public ApiResponse<List<RevenueStatisticResponse>> getYearlyRevenue(@RequestParam(defaultValue = "2025") int year) {
        return ApiResponse.<List<RevenueStatisticResponse>>builder().status(200).message("Success").data(revenueService.getYearlyRevenueStatistics(year)).build();
    }

    @Override
    public ApiResponse<List<RevenueStatisticResponse>> getRevenueByDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return ApiResponse.<List<RevenueStatisticResponse>>builder().status(400).message("Start date must be before end date").build();
        }
        return ApiResponse.<List<RevenueStatisticResponse>>builder().status(200).message("Success").data(revenueService.getRevenueStatisticsByDateRange(startDate, endDate)).build();
    }
}
