package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.entity.Amenity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AmenityMapper {

    AmenityResponse toAmenityResponse(Amenity amenity);

    List<AmenityResponse> toAmenityResponseList(List<Amenity> amenities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotelAmenities", ignore = true)
    @Mapping(target = "roomAmenities", ignore = true)
    Amenity toAmenity(AmenityRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotelAmenities", ignore = true)
    @Mapping(target = "roomAmenities", ignore = true)
    void updateAmenityFromRequest(AmenityRequest request, @MappingTarget Amenity amenity);
}
