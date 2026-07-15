package org.example.hotelbookingservice.services;

import org.example.hotelbookingservice.dto.request.hotel.operations.*;
import org.example.hotelbookingservice.dto.response.hotel.operations.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IHotelOperationsService {
    HotelResponse createHotel(HotelCreateRequest request, Authentication authentication);
    PaginatedResponse<HotelResponse> listPublicHotels(int limit, int offset);
    PaginatedResponse<HotelResponse> listManageableHotels(int limit, int offset, Authentication authentication);
    HotelResponse publicHotelDetail(UUID hotelId);
    HotelResponse managementHotelDetail(UUID hotelId, Authentication authentication);
    HotelResponse updateHotel(UUID hotelId, HotelUpdateRequest request, Authentication authentication);
    HotelResponse changeHotelStatus(UUID hotelId, String requestedStatus, Authentication authentication);
    HotelResponse archiveHotel(UUID hotelId, Authentication authentication);
    List<HotelMemberResponse> listMembers(UUID hotelId, Authentication authentication);
    List<HotelMemberCandidateResponse> listMemberCandidates(UUID hotelId, String q, Authentication authentication);
    List<HotelMemberResponse> addMembers(UUID hotelId, List<UUID> accountIds, Authentication authentication);
    void removeMember(UUID hotelId, UUID accountId, Authentication authentication);
    List<AmenityResponse> listAmenities(Boolean active);
    AmenityResponse amenityDetail(UUID amenityId);
    AmenityResponse createAmenity(AmenityRequest request, Authentication authentication);
    AmenityResponse updateAmenity(UUID amenityId, AmenityRequest request, Authentication authentication);
    AmenityResponse disableAmenity(UUID amenityId, Authentication authentication);
    RoomTypeResponse createRoomType(UUID hotelId, RoomTypeRequest request, Authentication authentication);
    List<RoomTypeResponse> listRoomTypes(UUID hotelId, boolean management);
    RoomTypeResponse roomTypeDetail(UUID hotelId, UUID roomTypeId, boolean publicOnly);
    RoomTypeResponse updateRoomType(UUID hotelId, UUID roomTypeId, RoomTypeRequest request, Authentication authentication);
    void deleteRoomType(UUID hotelId, UUID roomTypeId, Authentication authentication);
    RoomResponse createRoom(UUID hotelId, RoomRequest request, Authentication authentication);
    List<RoomResponse> listRooms(UUID hotelId, Authentication authentication);
    RoomResponse getRoom(UUID hotelId, UUID roomId, Authentication authentication);
    RoomResponse updateRoomCondition(UUID hotelId, UUID roomId, RoomConditionRequest request, Authentication authentication);
    RoomResponse updateRoom(UUID hotelId, UUID roomId, RoomRequest request, Authentication authentication);
    void deleteRoom(UUID hotelId, UUID roomId, Authentication authentication);
    InventoryResponse upsertInventory(UUID hotelId, UUID roomTypeId, InventoryRequest request, Authentication authentication);
    List<InventoryResponse> bulkSetInventory(UUID hotelId, UUID roomTypeId, BulkInventoryRequest request, Authentication authentication);
    List<InventoryResponse> listInventory(UUID hotelId, UUID roomTypeId, LocalDate from, LocalDate to, Authentication authentication);
    void deleteInventory(UUID hotelId, UUID roomTypeId, UUID inventoryId, Authentication authentication);
    PaginatedResponse<AvailabilityResponse> publicAvailability(UUID hotelId, LocalDate from, LocalDate to, int limit, int offset);
}
