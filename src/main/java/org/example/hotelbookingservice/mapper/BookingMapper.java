package org.example.hotelbookingservice.mapper;

import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.dto.response.GuestDetailResponse;
import org.example.hotelbookingservice.dto.response.HotelResponse;

import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.entity.Booking;
import org.example.hotelbookingservice.entity.GuestDetail;
import org.example.hotelbookingservice.entity.Hotel;
import org.example.hotelbookingservice.entity.Room;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring", uses = {HotelMapper.class, UserMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class BookingMapper {

    // 1. Entity -> Response (Output)
    @Mapping(target = "customerEmail", source = "user.email")
    @Mapping(target = "customerPhone", source = "user.phone")
    @Mapping(target = "hotel", expression = "java(mapHotel(booking))")
    @Mapping(target = "rooms", expression = "java(mapBookedRooms(booking))")
    @Mapping(target = "guestDetails", source = "guestDetails", qualifiedByName = "mapGuestDetails")
    public abstract BookingResponse toBookingResponse(Booking booking);

    // 2. Request -> Entity (Input)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "bookingReference", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bookingrooms", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "refund", ignore = true)
    @Mapping(target = "cancelReason", ignore = true)
    @Mapping(target = "roomNumber", ignore = true)
    @Mapping(target = "damageFee", ignore = true)
    @Mapping(target = "damageDescription", ignore = true)
    @Mapping(target = "guestDetails", ignore = true)
    @Mapping(target = "guestEmail", ignore = true)
    @Mapping(target = "guestPhone", ignore = true)
    @Mapping(target = "subtotalAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "commissionPackageCode", ignore = true)
    @Mapping(target = "commissionRate", ignore = true)
    @Mapping(target = "commissionAmount", ignore = true)
    @Mapping(target = "pendingExpiresAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "noShowAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "checkIn", ignore = true)
    public abstract Booking toBooking(BookingCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingReference", ignore = true)
    @Mapping(target = "checkinDate", ignore = true)
    @Mapping(target = "checkoutDate", ignore = true)
    @Mapping(target = "adultAmount", ignore = true)
    @Mapping(target = "childrenAmount", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bookingrooms", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "refund", ignore = true)
    @Mapping(target = "specialRequire", ignore = true)
    @Mapping(target = "guestDetails", ignore = true)
    @Mapping(target = "guestEmail", ignore = true)
    @Mapping(target = "guestPhone", ignore = true)
    @Mapping(target = "subtotalAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "commissionPackageCode", ignore = true)
    @Mapping(target = "commissionRate", ignore = true)
    @Mapping(target = "commissionAmount", ignore = true)
    @Mapping(target = "pendingExpiresAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "noShowAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "promotionId", ignore = true)
    @Mapping(target = "checkIn", ignore = true)
    public abstract void updateBookingFromRequest(BookingUpdateRequest request, @MappingTarget Booking booking);


    public abstract List<BookingResponse> toBookingResponseList(List<Booking> bookings);


    protected HotelResponse mapHotel(Booking booking) {
        if (booking.getBookingrooms() != null && !booking.getBookingrooms().isEmpty()) {
            var hotel = booking.getBookingrooms().iterator().next().getRoom().getHotel();
            return HotelResponse.builder()
                    .id(hotel.getId())
                    .name(hotel.getName())
                    .location(hotel.getLocation())
                    .description(hotel.getDescription())
                    .starRating(hotel.getStarRating())
                    .email(hotel.getEmail())
                    .phone(hotel.getPhone())
                    .isActive("ACTIVE".equals(hotel.getStatus()))
                    .rooms(null)
                    .build();
        }
        return null;
    }

    protected List<RoomResponse> mapBookedRooms(Booking booking) {
        if (booking.getBookingrooms() == null || booking.getBookingrooms().isEmpty()) {
            return null;
        }
        return booking.getBookingrooms().stream()
                .map(br -> toRoomResponse(br.getRoom()))
                .collect(Collectors.toList());
    }

    @Mapping(target = "roomImages", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "availableQuantity", ignore = true)
    protected abstract RoomResponse toRoomResponse(Room room);

    @Named("mapGuestDetails")
    protected List<GuestDetailResponse> mapGuestDetails(Set<GuestDetail> guestDetails) {
        if (guestDetails == null || guestDetails.isEmpty()) return null;
        return guestDetails.stream()
                .map(g -> GuestDetailResponse.builder()
                        .id(g.getId())
                        .fullName(g.getFullName())
                        .identityNumber(g.getIdentityNumber())
                        .build())
                .collect(Collectors.toList());
    }

}
