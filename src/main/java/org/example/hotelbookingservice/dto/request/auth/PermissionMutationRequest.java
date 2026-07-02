package org.example.hotelbookingservice.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionMutationRequest {
    @NotBlank
    @Size(max = 120)
    private String key;
    @NotBlank
    @Size(max = 120)
    private String name;
    private String description;
}
