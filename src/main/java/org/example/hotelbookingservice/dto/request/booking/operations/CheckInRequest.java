package org.example.hotelbookingservice.dto.request.booking.operations;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CheckInRequest(
        @Size(max = 1000) String note,
        @NotNull @Valid CheckInGuestRequest primary,
        List<@Valid CheckInGuestRequest> companions
) {
}
