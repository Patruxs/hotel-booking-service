package org.example.hotelbookingservice.dto.request.user;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminUserUpdateRequest(
        @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Size(max = 20) String phone,
        LocalDate dob
) {
}
