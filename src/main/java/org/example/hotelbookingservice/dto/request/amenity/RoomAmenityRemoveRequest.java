package org.example.hotelbookingservice.dto.request.amenity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoomAmenityRemoveRequest {

    @NotNull(message = "Danh sách tiện ích không được để null")
    @NotEmpty(message = "Danh sách tiện ích cần xóa không được rỗng")
    @Schema(description = "Danh sách ID các tiện ích cần xóa khỏi phòng", example = "[3, 4]")
    private List<Integer> amenityIds;
}
