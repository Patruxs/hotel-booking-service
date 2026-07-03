package org.example.hotelbookingservice.dto.operations;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HotelOperationsDtos {
    private HotelOperationsDtos() {
    }

    public record PaginatedResponse<T>(
            List<T> data,
            PageMeta meta
    ) {
    }

    public record PageMeta(
            int limit,
            int offset,
            long total
    ) {
    }

    public record HotelCreateRequest(
            UUID ownerId,
            @NotBlank @Size(max = 160) String name,
            String description,
            @Size(max = 1000) String address,
            @Size(max = 120) String city,
            @Size(max = 120) String country,
            @Email @Size(max = 320) String email,
            @Size(max = 32) String phone,
            @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal starRating
    ) {
    }

    public record HotelUpdateRequest(
            @Size(max = 160) String name,
            String description,
            @Size(max = 1000) String address,
            @Size(max = 120) String city,
            @Size(max = 120) String country,
            @Email @Size(max = 320) String email,
            @Size(max = 32) String phone,
            @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal starRating
    ) {
    }

    public record HotelStatusRequest(
            @NotBlank String status
    ) {
    }

    public record HotelResponse(
            UUID id,
            UUID ownerId,
            String name,
            String slug,
            String description,
            String address,
            String city,
            String country,
            String email,
            String phone,
            String status,
            BigDecimal starRating,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt,
            List<String> allowedActions
    ) {
    }

    public record MemberMutationRequest(
            @JsonAlias("userIds") @NotNull List<UUID> accountIds
    ) {
    }

    public record HotelMemberResponse(
            UUID hotelId,
            UUID accountId,
            String email,
            String firstName,
            String lastName,
            Instant createdAt,
            boolean owner
    ) {
    }

    public record AmenityRequest(
            @Size(max = 120) String key,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 64) String type,
            Boolean active
    ) {
    }

    public record AmenityResponse(
            UUID id,
            String key,
            String name,
            String type,
            boolean active,
            boolean system,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record RoomTypeRequest(
            @Size(max = 160) String name,
            String description,
            @JsonProperty("price_per_night") BigDecimal pricePerNightSnake,
            BigDecimal pricePerNight,
            @JsonProperty("max_guests") Integer maxGuestsSnake,
            Integer maxGuests,
            Integer numberOfBedrooms,
            List<UUID> amenityIds
    ) {
        public BigDecimal resolvedPricePerNight() {
            return pricePerNightSnake != null ? pricePerNightSnake : pricePerNight;
        }

        public Integer resolvedMaxGuests() {
            return maxGuestsSnake != null ? maxGuestsSnake : maxGuests;
        }
    }

    public record RoomTypeResponse(
            UUID id,
            UUID hotelId,
            String name,
            String description,
            @JsonProperty("price_per_night") BigDecimal pricePerNight,
            @JsonProperty("max_guests") int maxGuests,
            int numberOfBedrooms,
            boolean active,
            Instant deletedAt,
            Instant createdAt,
            Instant updatedAt,
            List<AmenityResponse> amenities
    ) {
    }

    public record RoomRequest(
            UUID roomTypeId,
            @Size(max = 40) String roomNumber,
            String condition,
            Boolean active
    ) {
    }

    public record RoomResponse(
            UUID id,
            UUID hotelId,
            UUID roomTypeId,
            String roomNumber,
            String condition,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record InventoryRequest(
            @NotNull LocalDate date,
            @NotNull @Min(0) Integer totalRooms,
            @NotNull @Min(0) Integer availableRooms,
            Boolean stopSell
    ) {
    }

    public record InventoryResponse(
            UUID id,
            UUID hotelId,
            UUID roomTypeId,
            LocalDate date,
            int totalRooms,
            int availableRooms,
            boolean stopSell,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record AvailabilityResponse(
            UUID id,
            UUID hotelId,
            String name,
            String description,
            @JsonProperty("price_per_night") BigDecimal pricePerNight,
            @JsonProperty("max_guests") int maxGuests,
            int numberOfBedrooms,
            int availableRooms,
            List<AmenityResponse> amenities
    ) {
    }
}
