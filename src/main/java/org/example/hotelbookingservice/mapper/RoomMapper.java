package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.Amenity;
import org.example.hotelbookingservice.entity.Image;
import org.example.hotelbookingservice.entity.Room;
import org.example.hotelbookingservice.entity.Roomamenity;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AmenityMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class RoomMapper {

    //Response
    @Mapping(target = "roomImages", source = "images", qualifiedByName = "mapImages")
    @Mapping(target = "amenities", source = "roomAmenities", qualifiedByName = "mapAmenities")
    @Mapping(target = "availableQuantity", ignore = true)
    public abstract RoomResponse toRoomResponse(Room room);

    public abstract List<RoomResponse> toRoomResponseList(List<Room> rooms);


    //Request
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "roomAmenities", ignore = true)
    @Mapping(target = "bookingrooms", ignore = true)
    @Mapping(target = "physicalRooms", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    public abstract Room toRoom(RoomCreateRequest roomCreateRequest);

    //Update Entity from Request
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "roomAmenities", ignore = true)
    @Mapping(target = "bookingrooms", ignore = true)
    @Mapping(target = "physicalRooms", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    public abstract void updateRoomFromRequest(RoomCreateRequest request, @MappingTarget Room room);


    @Named("amenityOnly")
    @BeanMapping(ignoreByDefault = true)
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
                .map(ra -> toAmenityResponse(ra.getAmenity()))
                .collect(Collectors.toList());
    }

    protected abstract AmenityResponse toAmenityResponse(Amenity amenity);

}
