package org.example.hotelbookingservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.example.hotelbookingservice.entity.Role}
 */
@Value
public class RoleResponse implements Serializable {

    Integer id;

    @NotBlank(message = "Role name is required")
    @Size(max = 20, message = "Role name must be less than 20 characters")
    String name;
}