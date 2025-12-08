package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class RoomMapper {

    @Autowired
    protected AmenityMapper amenityMapper;

    //Response
    @Mapping(target = "roomImages", source = "images", qualifiedByName = "mapImages")
    @Mapping(target = "amenities", source = "roomAmenities", qualifiedByName = "mapAmenities")
    public abstract RoomResponse toRoomResponse(Room room);

    public abstract List<RoomResponse> toRoomResponseList(List<Room> rooms);


    //Request
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "roomAmenities", ignore = true)
    @Mapping(target = "bookingrooms", ignore = true)
    public abstract Room toRoom(RoomCreateRequest roomCreateRequest);

    //Update Entity from Request
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "roomAmenities", ignore = true)
    @Mapping(target = "bookingrooms", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract void updateRoomFromRequest(RoomCreateRequest request, @MappingTarget Room room);


    @Named("amenityOnly")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "amenities", source = "roomAmenities", qualifiedByName = "mapAmenities")
    public abstract RoomResponse toRoomAmenityOnly(Room room);

    @IterableMapping(qualifiedByName = "amenityOnly")
    public abstract List<RoomResponse> toRoomAmenityOnlyList(List<Room> rooms);


    // Logic to get list of image paths
    @Named("mapImages")
    protected List<String> mapImages(Set<Image> images) {
        if (images == null) return null;
        return images.stream().map(Image::getPath).collect(Collectors.toList());
    }

    // Logic to get amenity name
    @Named("mapAmenities")
    protected List<AmenityResponse> mapAmenities(Set<Roomamenity> roomAmenities) {
        if (roomAmenities == null || roomAmenities.isEmpty()) return null;
        return roomAmenities.stream()
                .map(ra -> amenityMapper.toAmenityResponse(ra.getAmenity()))
                .collect(Collectors.toList());
    }

}
