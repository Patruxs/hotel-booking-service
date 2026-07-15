package org.example.hotelbookingservice.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.hotelbookingservice.enums.BookingStatus;

@Data
public class BookingUpdateRequest {
    @Schema(description = "New booking status (CHECKED_IN, CHECKED_OUT, CANCELLED)", example = "CHECKED_IN")
    private BookingStatus status;
    @Schema(description = "Cancellation reason (if any)", example = "Guest did not show up")
    private String cancelReason;
    @Schema(description = "Room number assigned to guest during check-in", example = "205")
    private String roomNumber;

    @Schema(description = "Damage fee (if any, applied during check-out)", example = "500000.0")
    private Float damageFee;
    @Schema(description = "Damage description", example = "Broken bathroom mirror")
    private String damageDescription;
}
