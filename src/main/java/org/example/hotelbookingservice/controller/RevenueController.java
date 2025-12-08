package org.example.hotelbookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Revenue Management", description = "Quản lý thống kê doanh thu hệ thống")
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
            @RequestParam(defaultValue = "2025") int year
    ) {
        return ApiResponse.<List<RevenueStatisticResponse>>builder()
                .status(200)
                .message("Success")
                .data(revenueService.getYearlyRevenueStatistics(year))
                .build();
    }


    @Operation(
            summary = "Thống kê doanh thu theo khoảng thời gian (ADMIN)",
            description = "Trả về tổng doanh thu, số lượng booking và hoa hồng của từng khách sạn trong khoảng thời gian được chọn."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RevenueStatisticResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Lỗi dữ liệu đầu vào (Ngày bắt đầu lớn hơn ngày kết thúc...)",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập (Yêu cầu quyền ADMIN)",
                    content = @Content
            )
    })
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