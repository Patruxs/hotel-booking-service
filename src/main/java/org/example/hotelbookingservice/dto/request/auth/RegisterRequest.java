package org.example.hotelbookingservice.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.UserRole;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Fullname is required")
    @Size(min = 2, max = 50, message = "Fullname must be between 2 and 50 characters")
    @Schema(description = "User's full name", example = "Nguyen Van A")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address (used for login)", example = "customer@example.com")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Phone number must be between 10 and 12 digits")
    @Schema(description = "Contact phone number", example = "0987654321")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(description = "Password (minimum 8 characters)", example = "Password1!")
    private String password;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth", example = "1995-05-20", type = "string", pattern = "yyyy-MM-dd")
    private LocalDate dob;

}
