package org.example.hotelbookingservice.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 40, message = "Full name must be less than 40 characters")
    @Schema(description = "New full name", example = "Nguyen Van A Updated")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Phone number must be between 10 and 12 digits")
    @Schema(description = "New phone number", example = "0912345678")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "1995-10-20")
    private LocalDate dob;
}
