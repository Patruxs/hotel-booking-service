package org.example.hotelbookingservice.dto.request.hotel;

import lombok.Data;

@Data
public class HotelUpdateRequest {
    private String name;
    private String location;
    private String description;
    private String email;
    private String phone;
    private String contactName;
    private String contactPhone;
    private Boolean isActive;
}
