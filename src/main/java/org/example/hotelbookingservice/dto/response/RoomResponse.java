package org.example.hotelbookingservice.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hotelbookingservice.enums.RoomType;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomResponse {
    private Integer id;
    private RoomType type;
    private BigDecimal price;
    private Integer capacity;
    private String description;
    private String name;
    private Integer amount;
    private Integer availableQuantity;

    private List<String> roomImages;
    private List<AmenityResponse> amenities;
}
