package org.example.hotelbookingservice.dto.request.hotel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class HotelCreateRequest {
    @NotBlank(message = "Hotel name is required")
    @Schema(description = "Tên khách sạn", example = "Grand Saigon Hotel")
    private String name;

    @NotBlank(message = "Location is required")
    @Schema(description = "Địa chỉ / Thành phố", example = "Ho Chi Minh City")
    private String location;

    @NotBlank(message = "Description is required")
    @Schema(description = "Mô tả khách sạn", example = "Khách sạn 5 sao trung tâm thành phố...")
    private String description;

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must be at most 5")
    @Schema(description = "Số sao (1-5)", example = "5")
    private Integer starRating;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email liên hệ của khách sạn", example = "contact@grandsaigon.com")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Phone number must be between 10 and 12 digits")
    @Schema(description = "Số điện thoại khách sạn", example = "02839123456")
    private String phone;

    @NotBlank(message = "Contact name is required")
    @Schema(description = "Tên người liên hệ đại diện", example = "Mr. Quan Ly")
    private String contactName;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Contact phone must be between 10 and 12 digits")
    @Schema(description = "Số điện thoại người liên hệ", example = "0909123456")
    private String contactPhone;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive = false;

    @Schema(description = "Danh sách ID các tiện ích của khách sạn", example = "[1, 2, 3]")
    private List<Integer> amenityIds;

}
