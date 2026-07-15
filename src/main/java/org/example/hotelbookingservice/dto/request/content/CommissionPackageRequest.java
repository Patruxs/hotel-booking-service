package org.example.hotelbookingservice.dto.request.content;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CommissionPackageRequest(
        String code,
        @Size(max = 120) String name,
        String description,
        BigDecimal commissionRate,
        @JsonAlias("isActive") Boolean active
) {
}
