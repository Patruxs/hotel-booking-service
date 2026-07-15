package org.example.hotelbookingservice.dto.request.physicalroom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.RoomCondition;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to create/update a physical room")
public class PhysicalRoomCreateRequest {

    @NotNull(message = "Room number is required")
    @Size(max = 40, message = "Room number must be at most 40 characters")
    @Schema(description = "Physical room number", example = "D101")
    private String roomNumber;

    @NotNull(message = "Room condition is required")
    @Schema(description = "Room condition (CLEAN, DIRTY, MAINTENANCE)", example = "CLEAN")
    private RoomCondition roomCondition;

    @NotNull(message = "Room ID is required")
    @Schema(description = "ID of the room type that this physical room belongs to", example = "1")
    private Integer roomId;
}
