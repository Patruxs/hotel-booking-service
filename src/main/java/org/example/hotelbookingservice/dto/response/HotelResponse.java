package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotelResponse {
    private Integer id;
    private UUID uuid;
    private String name;
    private String location;
    private String description;
    private Integer starRating;
    private String email;
    private String phone;
    private Boolean isActive;
    private String contactName;
    private String contactPhone;

    private String coverImage;
    private Double averageRating;
    private Integer totalReviews;
    private BigDecimal minPrice;
    private Boolean isFavorite;

    private List<String> images;
    private List<String> amenities;
    private List<RoomResponse> rooms;
}
