package org.example.hotelbookingservice.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Staying guest details")
public class GuestDetailRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    @Schema(description = "Guest full name", example = "Nguyen Van A")
    private String fullName;

    @NotBlank(message = "Identity number is required")
    @Size(max = 20)
    @Schema(description = "Citizen ID/Passport number", example = "079203012345")
    private String identityNumber;
}
