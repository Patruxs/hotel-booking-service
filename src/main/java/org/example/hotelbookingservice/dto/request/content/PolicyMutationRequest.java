package org.example.hotelbookingservice.dto.request.content;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PolicyMutationRequest(
        @NotBlank @Pattern(regexp = "CHECKIN|CANCELLATION|PAYMENT|CHILDREN|PET|SMOKING|GENERAL") String type,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 5000) String content,
        Boolean enabled,
        @Min(0) @JsonAlias("order") Integer sortOrder
) {
}
