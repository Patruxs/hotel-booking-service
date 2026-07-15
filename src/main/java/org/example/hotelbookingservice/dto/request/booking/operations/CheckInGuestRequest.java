package org.example.hotelbookingservice.dto.request.booking.operations;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CheckInGuestRequest(
        UUID userId,
        @NotBlank @Size(max = 120) String fullName,
        @JsonAlias({"idNumber", "identityNumber"}) @Size(max = 40) String identityNumber,
        @Size(max = 32) String phone,
        String email,
        String nationality,
        String dateOfBirth,
        String note
) {
}
