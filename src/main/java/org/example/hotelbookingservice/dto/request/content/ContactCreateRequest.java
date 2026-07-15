package org.example.hotelbookingservice.dto.request.content;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Email @Size(max = 320) String email,
        @Size(max = 32) String phone,
        @Size(max = 180) String subject,
        @NotBlank String message
) {
}
