package org.example.hotelbookingservice.dto.request.hotel.operations;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RoomRequest(
        UUID roomTypeId,
        @Size(max = 40) String roomNumber,
        String condition,
        Boolean active
) {
}
