package org.example.hotelbookingservice.dto.request.hotel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class HotelCreateRequest {
    @NotBlank(message = "Hotel name is required")
    @Schema(description = "Hotel name", example = "Grand Saigon Hotel")
    private String name;

    @NotBlank(message = "Location is required")
    @Schema(description = "Address / City", example = "Ho Chi Minh City")
    private String location;

    @NotBlank(message = "Description is required")
    @Schema(description = "Hotel description", example = "Khách sạn 5 sao trung tâm thành phố...")
    private String description;

    @NotNull(message = "Star rating is required")
    @Min(value = 1, message = "Star rating must be at least 1")
    @Max(value = 5, message = "Star rating must be at most 5")
    @Schema(description = "Star rating (1-5)", example = "5")
    private Integer starRating;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Contact email of the hotel", example = "contact@grandsaigon.com")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Phone number must be between 10 and 12 digits")
    @Schema(description = "Hotel phone number", example = "02839123456")
    private String phone;

    @NotBlank(message = "Contact name is required")
    @Schema(description = "Representative contact person's name", example = "Mr. Quan Ly")
    private String contactName;

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\d{10,12}$", message = "Contact phone must be between 10 and 12 digits")
    @Schema(description = "Contact person's phone number", example = "0909123456")
    private String contactPhone;

    @Schema(description = "Active status", example = "true")
    private Boolean isActive = false;

    @Schema(description = "List of IDs of hotel amenities", example = "[1, 2, 3]")
    private List<Integer> amenityIds;

}
