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
    void updateHotelFromRequest(HotelUpdateRequest request, @MappingTarget Hotel hotel);

    // 1. Lấy danh sách URL ảnh từ Set<Image>
    @Named("mapHotelImages")
    default List<String> mapHotelImages(Set<Image> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .map(Image::getPath) // Lấy đường dẫn ảnh
                .collect(Collectors.toList());
    }

    // 2. Lấy danh sách tên tiện ích từ Set<Hotelamenity>
    @Named("mapHotelAmenities")
    default List<String> mapHotelAmenities(Set<Hotelamenity> hotelAmenities) {
        if (hotelAmenities == null || hotelAmenities.isEmpty()) return null;
        return hotelAmenities.stream()
                .map(ha -> ha.getAmenity().getName()) // Truy cập Hotelamenity -> Amenity -> Name
                .collect(Collectors.toList());
    }

    // Logic lấy ảnh bìa (ảnh đầu tiên)
    @Named("mapCoverImage")
    default String mapCoverImage(Set<Image> images) {
        if (images == null || images.isEmpty()) return null;
        return images.iterator().next().getPath();
    }

    // Logic tính điểm trung bình
    @Named("calcAverageRating")
    default Double calcAverageRating(Set<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToDouble(Review::getPoint)
                .average()
                .orElse(0.0);
    }

    // Logic tìm giá thấp nhất
    @Named("calcMinPrice")
    default BigDecimal calcMinPrice(Set<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) return BigDecimal.ZERO;
        return rooms.stream()
                .map(Room::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }


}