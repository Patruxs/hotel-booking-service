package org.example.hotelbookingservice.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.response.RevenueStatisticResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/v1/revenue")
@Tag(name = "Revenue Management", description = "Management of system revenue statistics")
public interface RevenueApi {

    @Operation(summary = "Yearly revenue statistics (ADMIN, MANAGER)", description = "View hotel revenue by month.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied", content = @Content) })
    @GetMapping("/yearly")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    ApiResponse<List<RevenueStatisticResponse>> getYearlyRevenue(@RequestParam(defaultValue = "2025") int year);

    @Operation(summary = "Revenue statistics by date range (ADMIN, MANAGER)", description = "Returns the total revenue, number of bookings, and commission of each hotel within the selected date range.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RevenueStatisticResponse.class))), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data (Start date is after end date...)", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied (ADMIN or MANAGER authority required)", content = @Content) })
    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('MANAGER')")
    ApiResponse<List<RevenueStatisticResponse>> getRevenueByDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
