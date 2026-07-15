package org.example.hotelbookingservice.dto.request.hotel.operations;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record MemberMutationRequest(
        @JsonAlias("userIds") @NotNull List<UUID> accountIds
) {
}
