package org.example.hotelbookingservice.dto.request.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateRequest {
    @Schema(hidden = true)
    private Integer id;

    @NotNull(message = "Hotel ID is required")
    @Schema(description = "ID của khách sạn muốn thêm phòng", example = "1")
    private Integer hotelId;


    @NotNull(message = "Room type is required")
    @Schema(description = "Loại phòng", example = "SINGLE")
    private RoomType type;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Schema(description = "Giá phòng mỗi đêm", example = "500000")
    private BigDecimal price;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1 person")
    @Schema(description = "Sức chứa tối đa (người)", example = "2")
    private Integer capacity;

    @NotBlank(message = "Description is required")
    @Schema(description = "Mô tả chi tiết tiện nghi", example = "Phòng có view biển, điều hòa 2 chiều...")
    private String description;

    @NotBlank(message = "Room name is required")
    @Schema(description = "Tên hiển thị của phòng", example = "Deluxe Ocean View")
    private String name;

    @Min(value = 1, message = "Amount of rooms must be at least 1")
    @Schema(description = "Số lượng phòng cùng loại này", example = "5")
    private Integer amount;

    @Schema(description = "Danh sách tiện ích đi kèm", example = "[1,2]")
    private List<Integer> amenityIds;
}
