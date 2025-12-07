package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueStatisticResponse {
    private Integer hotelId;
    private String hotelName;
    private Integer totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal adminCommission;
    private Integer month;
    private Integer year;
}