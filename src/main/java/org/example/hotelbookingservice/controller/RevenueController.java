package org.example.hotelbookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@RestController
@RequestMapping("/api/v1/revenue")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RevenueController {

    IRevenueService revenueService;

    @Operation(summary = "Thống kê doanh thu theo năm (ADMIN)", description = "Xem doanh thu của các khách sạn theo từng tháng.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping("/yearly")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<RevenueStatisticResponse>> getYearlyRevenue(
            @RequestParam(defaultValue = "2024") int year
    ) {
        return ApiResponse.<List<RevenueStatisticResponse>>builder()
                .status(200)
                .message("Success")
                .data(revenueService.getYearlyRevenueStatistics(year))
                .build();
    }


    @Operation(summary = "Thống kê doanh thu theo khoảng thời gian (ADMIN)",
            description = "Xem tổng doanh thu của từng khách sạn trong khoảng Start Date - End Date.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<List<RevenueStatisticResponse>> getRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate.isAfter(endDate)) {
            return ApiResponse.<List<RevenueStatisticResponse>>builder()
                    .status(400)
                    .message("Start date must be before end date")
                    .build();
        }

        return ApiResponse.<List<RevenueStatisticResponse>>builder()
                .status(200)
                .message("Success")
                .data(revenueService.getRevenueStatisticsByDateRange(startDate, endDate))
                .build();
    }
}