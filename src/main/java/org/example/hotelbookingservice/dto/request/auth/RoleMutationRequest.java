package org.example.hotelbookingservice.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleMutationRequest {
    @NotBlank
    @Size(max = 64)
    private String name;
    @Size(max = 120)
    private String displayName;
    private String description;
}
