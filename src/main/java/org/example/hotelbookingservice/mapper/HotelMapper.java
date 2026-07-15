package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.request.hotel.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.entity.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface HotelMapper {

    @Mapping(target = "images", source = "images", qualifiedByName = "mapHotelImages")
    @Mapping(target = "amenities", source = "hotelAmenities", qualifiedByName = "mapHotelAmenities")
    @Mapping(target = "rooms", source = "rooms")
    @Mapping(target = "coverImage", source = "images", qualifiedByName = "mapCoverImage")
    @Mapping(target = "averageRating", source = "reviews", qualifiedByName = "calcAverageRating")
    @Mapping(target = "totalReviews", expression = "java(hotel.getReviews() == null ? 0 : hotel.getReviews().size())")
    @Mapping(target = "minPrice", source = "rooms", qualifiedByName = "calcMinPrice")
    @Mapping(target = "isFavorite", ignore = true)
    HotelResponse toHotelResponse(Hotel hotel);

    List<HotelResponse> toHotelResponseList(List<Hotel> hotels);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hotelAmenities", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "policies", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    Hotel toHotel(HotelCreateRequest request);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hotelAmenities", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "policies", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "starRating", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    void updateHotelFromRequest(HotelUpdateRequest request, @MappingTarget Hotel hotel);

    // 1. Get list of image URLs from Set<Image>
    @Named("mapHotelImages")
    default List<String> mapHotelImages(Set<Image> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .map(Image::getPath) // Get image path
                .collect(Collectors.toList());
    }

    // 2. Get list of amenity names from Set<Hotelamenity>
    @Named("mapHotelAmenities")
    default List<String> mapHotelAmenities(Set<Hotelamenity> hotelAmenities) {
        if (hotelAmenities == null || hotelAmenities.isEmpty()) return java.util.Collections.emptyList();
        return hotelAmenities.stream()
                .map(Hotelamenity::getAmenity)
                .filter(a -> a != null)
                .map(Amenity::getName)
                .collect(Collectors.toList());
    }

    // Logic to get cover image (first image)
    @Named("mapCoverImage")
    default String mapCoverImage(Set<Image> images) {
        if (images == null || images.isEmpty()) return null;
        return images.iterator().next().getPath();
    }

    // Logic to calculate average rating
    @Named("calcAverageRating")
    default Double calcAverageRating(Set<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToDouble(Review::getPoint)
                .average()
                .orElse(0.0);
    }

    // Logic to find minimum price
    @Named("calcMinPrice")
    default BigDecimal calcMinPrice(Set<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) return BigDecimal.ZERO;
        return rooms.stream()
                .map(Room::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }


}
