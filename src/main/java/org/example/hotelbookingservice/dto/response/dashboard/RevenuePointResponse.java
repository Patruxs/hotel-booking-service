package org.example.hotelbookingservice.dto.response.dashboard;

import java.math.BigDecimal;

public record RevenuePointResponse(String period, String month, BigDecimal revenue) {
}
