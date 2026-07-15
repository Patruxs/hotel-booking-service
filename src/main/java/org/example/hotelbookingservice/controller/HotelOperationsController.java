package org.example.hotelbookingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.AmenityRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.AmenityResponse;
import org.example.hotelbookingservice.dto.response.hotel.operations.AvailabilityResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.HotelCreateRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.HotelMemberCandidateResponse;
import org.example.hotelbookingservice.dto.response.hotel.operations.HotelMemberResponse;
import org.example.hotelbookingservice.dto.response.hotel.operations.HotelResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.HotelStatusRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.BulkInventoryRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.InventoryRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.InventoryResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.MemberMutationRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.PaginatedResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.RoomRequest;
import org.example.hotelbookingservice.dto.request.hotel.operations.RoomConditionRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.RoomResponse;
import org.example.hotelbookingservice.dto.request.hotel.operations.RoomTypeRequest;
import org.example.hotelbookingservice.dto.response.hotel.operations.RoomTypeResponse;
import org.example.hotelbookingservice.services.IHotelOperationsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Hotel Operations", description = "Live frontend hotel, member, room type, room, and inventory operations")
public class HotelOperationsController {
    private final IHotelOperationsService hotelOperationsService;

    @PostMapping("/hotels")
    @Operation(summary = "Create hotel")
    public ApiResponse<HotelResponse> createHotel(
            @RequestBody @Valid HotelCreateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(201)
                .message("Hotel created successfully")
                .data(hotelOperationsService.createHotel(request, authentication))
                .build();
    }

    @GetMapping("/hotels")
    @Operation(summary = "List public hotels")
    public ApiResponse<PaginatedResponse<HotelResponse>> listPublicHotels(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.<PaginatedResponse<HotelResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listPublicHotels(limit, offset))
                .build();
    }

    @GetMapping("/hotels/manageable")
    @Operation(summary = "List hotels manageable by the authenticated account")
    public ApiResponse<PaginatedResponse<HotelResponse>> listManageableHotels(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication
    ) {
        return ApiResponse.<PaginatedResponse<HotelResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listManageableHotels(limit, offset, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId:[0-9a-fA-F\\-]+}")
    @Operation(summary = "Get public hotel detail")
    public ApiResponse<HotelResponse> publicHotelDetail(@PathVariable UUID hotelId) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.publicHotelDetail(hotelId))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/manage")
    @Operation(summary = "Get management hotel detail")
    public ApiResponse<HotelResponse> managementHotelDetail(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.managementHotelDetail(hotelId, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId:[0-9a-fA-F\\-]+}")
    @Operation(summary = "Update hotel")
    public ApiResponse<HotelResponse> updateHotel(
            @PathVariable UUID hotelId,
            @RequestBody @Valid HotelUpdateRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel updated successfully")
                .data(hotelOperationsService.updateHotel(hotelId, request, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/status")
    @Operation(summary = "Update hotel status")
    public ApiResponse<HotelResponse> updateHotelStatus(
            @PathVariable UUID hotelId,
            @RequestBody @Valid HotelStatusRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel status updated successfully")
                .data(hotelOperationsService.changeHotelStatus(hotelId, request.status(), authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId:[0-9a-fA-F\\-]+}")
    @Operation(summary = "Archive hotel")
    public ApiResponse<HotelResponse> archiveHotel(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<HotelResponse>builder()
                .status(200)
                .message("Hotel archived successfully")
                .data(hotelOperationsService.archiveHotel(hotelId, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/members")
    @Operation(summary = "List hotel members")
    public ApiResponse<List<HotelMemberResponse>> listMembers(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<List<HotelMemberResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listMembers(hotelId, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/member-candidates")
    @Operation(summary = "List accounts that can be added to a hotel")
    public ApiResponse<List<HotelMemberCandidateResponse>> listMemberCandidates(
            @PathVariable UUID hotelId,
            @RequestParam(required = false) String q,
            Authentication authentication
    ) {
        return ApiResponse.<List<HotelMemberCandidateResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listMemberCandidates(hotelId, q, authentication))
                .build();
    }

    @PostMapping("/hotels/{hotelId}/members")
    @Operation(summary = "Add hotel members")
    public ApiResponse<List<HotelMemberResponse>> addMembers(
            @PathVariable UUID hotelId,
            @RequestBody @Valid MemberMutationRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<List<HotelMemberResponse>>builder()
                .status(200)
                .message("Hotel members updated successfully")
                .data(hotelOperationsService.addMembers(hotelId, request.accountIds(), authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/members/{accountId}")
    @Operation(summary = "Remove hotel member")
    public ApiResponse<Void> removeMember(
            @PathVariable UUID hotelId,
            @PathVariable UUID accountId,
            Authentication authentication
    ) {
        hotelOperationsService.removeMember(hotelId, accountId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Hotel member removed successfully")
                .build();
    }

    @GetMapping("/amenities")
    @Operation(summary = "List amenities")
    public ApiResponse<List<AmenityResponse>> listAmenities(@RequestParam(required = false) Boolean isActive) {
        return ApiResponse.<List<AmenityResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listAmenities(isActive))
                .build();
    }

    @GetMapping("/amenities/{amenityId}")
    @Operation(summary = "Get amenity detail")
    public ApiResponse<AmenityResponse> amenityDetail(@PathVariable UUID amenityId) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.amenityDetail(amenityId))
                .build();
    }

    @PostMapping("/amenities")
    @Operation(summary = "Create amenity")
    public ApiResponse<AmenityResponse> createAmenity(
            @RequestBody @Valid AmenityRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<AmenityResponse>builder()
                .status(201)
                .message("Amenity created successfully")
                .data(hotelOperationsService.createAmenity(request, authentication))
                .build();
    }

    @PutMapping("/amenities/{amenityId}")
    @Operation(summary = "Update amenity")
    public ApiResponse<AmenityResponse> updateAmenity(
            @PathVariable UUID amenityId,
            @RequestBody @Valid AmenityRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Amenity updated successfully")
                .data(hotelOperationsService.updateAmenity(amenityId, request, authentication))
                .build();
    }

    @DeleteMapping("/amenities/{amenityId}")
    @Operation(summary = "Disable amenity")
    public ApiResponse<AmenityResponse> disableAmenity(
            @PathVariable UUID amenityId,
            Authentication authentication
    ) {
        return ApiResponse.<AmenityResponse>builder()
                .status(200)
                .message("Amenity disabled successfully")
                .data(hotelOperationsService.disableAmenity(amenityId, authentication))
                .build();
    }

    @PostMapping("/hotels/{hotelId}/room-types")
    @Operation(summary = "Create room type")
    public ApiResponse<RoomTypeResponse> createRoomType(
            @PathVariable UUID hotelId,
            @RequestBody @Valid RoomTypeRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomTypeResponse>builder()
                .status(201)
                .message("Room type created successfully")
                .data(hotelOperationsService.createRoomType(hotelId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types")
    @Operation(summary = "List hotel room types")
    public ApiResponse<List<RoomTypeResponse>> listRoomTypes(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "false") boolean manage,
            Authentication authentication
    ) {
        if (manage) {
            hotelOperationsService.managementHotelDetail(hotelId, authentication);
        }
        return ApiResponse.<List<RoomTypeResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listRoomTypes(hotelId, manage))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types/available")
    @Operation(summary = "List available room types")
    public ApiResponse<PaginatedResponse<AvailabilityResponse>> listAvailability(
            @PathVariable UUID hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ApiResponse.<PaginatedResponse<AvailabilityResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.publicAvailability(hotelId, from, to, limit, offset))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types/{roomTypeId}")
    @Operation(summary = "Get room type detail")
    public ApiResponse<RoomTypeResponse> roomTypeDetail(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestParam(defaultValue = "false") boolean manage,
            Authentication authentication
    ) {
        if (manage) {
            hotelOperationsService.managementHotelDetail(hotelId, authentication);
        }
        return ApiResponse.<RoomTypeResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.roomTypeDetail(hotelId, roomTypeId, !manage))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/room-types/{roomTypeId}")
    @Operation(summary = "Update room type")
    public ApiResponse<RoomTypeResponse> updateRoomType(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestBody @Valid RoomTypeRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomTypeResponse>builder()
                .status(200)
                .message("Room type updated successfully")
                .data(hotelOperationsService.updateRoomType(hotelId, roomTypeId, request, authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/room-types/{roomTypeId}")
    @Operation(summary = "Delete room type")
    public ApiResponse<Void> deleteRoomType(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            Authentication authentication
    ) {
        hotelOperationsService.deleteRoomType(hotelId, roomTypeId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Room type deleted successfully")
                .build();
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    @Operation(summary = "Create room")
    public ApiResponse<RoomResponse> createRoom(
            @PathVariable UUID hotelId,
            @RequestBody @Valid RoomRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomResponse>builder()
                .status(201)
                .message("Room created successfully")
                .data(hotelOperationsService.createRoom(hotelId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId:[0-9a-fA-F\\-]+}/rooms")
    @Operation(summary = "List hotel rooms")
    public ApiResponse<List<RoomResponse>> listRooms(
            @PathVariable UUID hotelId,
            Authentication authentication
    ) {
        return ApiResponse.<List<RoomResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listRooms(hotelId, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId:[0-9a-fA-F\\-]+}/rooms/{roomId}")
    @Operation(summary = "Get hotel room detail")
    public ApiResponse<RoomResponse> roomDetail(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            Authentication authentication
    ) {
        return ApiResponse.<RoomResponse>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.getRoom(hotelId, roomId, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/rooms/{roomId}/condition")
    @Operation(summary = "Update physical room condition")
    public ApiResponse<RoomResponse> updateRoomCondition(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @RequestBody @Valid RoomConditionRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomResponse>builder()
                .status(200)
                .message("Room condition updated successfully")
                .data(hotelOperationsService.updateRoomCondition(hotelId, roomId, request, authentication))
                .build();
    }

    @PatchMapping("/hotels/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Update room")
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @RequestBody @Valid RoomRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<RoomResponse>builder()
                .status(200)
                .message("Room updated successfully")
                .data(hotelOperationsService.updateRoom(hotelId, roomId, request, authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/rooms/{roomId}")
    @Operation(summary = "Delete room")
    public ApiResponse<Void> deleteRoom(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            Authentication authentication
    ) {
        hotelOperationsService.deleteRoom(hotelId, roomId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Room deleted successfully")
                .build();
    }

    @PutMapping("/hotels/{hotelId}/room-types/{roomTypeId}/inventory")
    @Operation(summary = "Upsert room type inventory")
    public ApiResponse<InventoryResponse> upsertInventory(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestBody @Valid InventoryRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<InventoryResponse>builder()
                .status(200)
                .message("Inventory saved successfully")
                .data(hotelOperationsService.upsertInventory(hotelId, roomTypeId, request, authentication))
                .build();
    }

    @PutMapping("/hotels/{hotelId}/room-types/{roomTypeId}/inventory/bulk")
    @Operation(summary = "Bulk set room type inventory")
    public ApiResponse<List<InventoryResponse>> bulkSetInventory(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestBody @Valid BulkInventoryRequest request,
            Authentication authentication
    ) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .status(200)
                .message("Inventory range saved successfully")
                .data(hotelOperationsService.bulkSetInventory(hotelId, roomTypeId, request, authentication))
                .build();
    }

    @GetMapping("/hotels/{hotelId}/room-types/{roomTypeId}/inventory")
    @Operation(summary = "List room type inventory")
    public ApiResponse<List<InventoryResponse>> listInventory(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication
    ) {
        return ApiResponse.<List<InventoryResponse>>builder()
                .status(200)
                .message("Success")
                .data(hotelOperationsService.listInventory(hotelId, roomTypeId, from, to, authentication))
                .build();
    }

    @DeleteMapping("/hotels/{hotelId}/room-types/{roomTypeId}/inventory/{inventoryId}")
    @Operation(summary = "Delete eligible room type inventory")
    public ApiResponse<Void> deleteInventory(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomTypeId,
            @PathVariable UUID inventoryId,
            Authentication authentication
    ) {
        hotelOperationsService.deleteInventory(hotelId, roomTypeId, inventoryId, authentication);
        return ApiResponse.<Void>builder()
                .status(200)
                .message("Inventory deleted successfully")
                .build();
    }
}
