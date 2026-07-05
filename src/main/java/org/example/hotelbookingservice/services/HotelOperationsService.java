package org.example.hotelbookingservice.services;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AmenityRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AmenityResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.AvailabilityResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelCreateRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelMemberResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.HotelUpdateRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.InventoryRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.InventoryResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PageMeta;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PaginatedResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomResponse;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomTypeRequest;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.RoomTypeResponse;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelOperationsService {
    private static final Set<String> HOTEL_STATUSES = Set.of("DRAFT", "ACTIVE", "SUSPENDED", "ARCHIVED");
    private static final Set<String> ROOM_CONDITIONS = Set.of("CLEAN", "DIRTY", "MAINTENANCE", "OUT_OF_SERVICE");
    private static final Set<String> NON_TERMINAL_BOOKING_STATUSES = Set.of("PENDING", "CONFIRMED", "CHECKED_IN");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public HotelResponse createHotel(HotelCreateRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "hotels.create", null);
        if (request.ownerId() != null && !hasPermission(user, "security.manage")) {
            throw forbidden("Only platform administrators can choose a hotel owner");
        }
        UUID ownerId = request.ownerId() != null ? request.ownerId() : user.accountId();
        requireAccountExists(ownerId);

        UUID hotelId = UUID.randomUUID();
        String slug = uniqueHotelSlug(request.name());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", hotelId)
                .addValue("ownerId", ownerId)
                .addValue("name", trimRequired(request.name(), "Hotel name is required"))
                .addValue("slug", slug)
                .addValue("description", trimToNull(request.description()))
                .addValue("address", trimToNull(request.address()))
                .addValue("city", trimToNull(request.city()))
                .addValue("country", trimOrDefault(request.country(), "Vietnam"))
                .addValue("email", trimToNull(request.email()))
                .addValue("phone", trimToNull(request.phone()))
                .addValue("starRating", request.starRating());

        jdbcTemplate.update("""
                insert into hotels (
                    id, owner_id, name, slug, description, address, city, country, email, phone, star_rating, status
                ) values (
                    :id, :ownerId, :name, :slug, :description, :address, :city, :country, :email, :phone, :starRating, 'DRAFT'
                )
                """, params);
        jdbcTemplate.update("""
                insert into hotel_members (hotel_id, account_id)
                values (:hotelId, :accountId)
                on conflict do nothing
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("accountId", ownerId));

        return requireHotel(hotelId, true, user);
    }

    public PaginatedResponse<HotelResponse> listPublicHotels(int limit, int offset) {
        int boundedLimit = boundedLimit(limit);
        int boundedOffset = Math.max(offset, 0);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", boundedLimit)
                .addValue("offset", boundedOffset);

        String fromWhere = """
                from hotels h
                where h.status = 'ACTIVE'
                  and h.deleted_at is null
                  and exists (
                      select 1
                      from room_types rt
                      where rt.hotel_id = h.id
                        and rt.deleted_at is null
                  )
                """;
        long total = jdbcTemplate.queryForObject("select count(*) " + fromWhere, params, Long.class);
        List<HotelResponse> hotels = jdbcTemplate.query("""
                select h.*
                """ + fromWhere + """
                order by h.created_at desc, h.name
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapHotel(rs, null));

        return new PaginatedResponse<>(hotels, new PageMeta(boundedLimit, boundedOffset, total));
    }

    public HotelResponse publicHotelDetail(UUID hotelId) {
        return queryHotel("""
                select h.*
                from hotels h
                where h.id = :hotelId
                  and h.status = 'ACTIVE'
                  and h.deleted_at is null
                  and exists (
                      select 1
                      from room_types rt
                      where rt.hotel_id = h.id
                        and rt.deleted_at is null
                  )
                """, hotelId, null);
    }

    public HotelResponse managementHotelDetail(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanViewHotel(hotelId, user);
        return requireHotel(hotelId, false, user);
    }

    @Transactional
    public HotelResponse updateHotel(UUID hotelId, HotelUpdateRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "hotels.manage", hotelId);
        HotelResponse current = requireHotel(hotelId, false, user);

        jdbcTemplate.update("""
                update hotels
                set name = :name,
                    description = :description,
                    address = :address,
                    city = :city,
                    country = :country,
                    email = :email,
                    phone = :phone,
                    star_rating = :starRating,
                    updated_at = now()
                where id = :hotelId
                """, new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("name", trimOrDefault(request.name(), current.name()))
                .addValue("description", request.description() == null ? current.description() : trimToNull(request.description()))
                .addValue("address", request.address() == null ? current.address() : trimToNull(request.address()))
                .addValue("city", request.city() == null ? current.city() : trimToNull(request.city()))
                .addValue("country", trimOrDefault(request.country(), current.country()))
                .addValue("email", request.email() == null ? current.email() : trimToNull(request.email()))
                .addValue("phone", request.phone() == null ? current.phone() : trimToNull(request.phone()))
                .addValue("starRating", request.starRating() == null ? current.starRating() : request.starRating()));
        return requireHotel(hotelId, false, user);
    }

    @Transactional
    public HotelResponse changeHotelStatus(UUID hotelId, String requestedStatus, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        HotelResponse current = requireHotel(hotelId, false, user);
        requireAction(user, "hotels.status.update", hotelId);
        String nextStatus = normalizeStatus(requestedStatus);
        validateHotelStatusTransition(current.status(), nextStatus);
        if ("ARCHIVED".equals(nextStatus)) {
            rejectArchiveWithNonTerminalBookings(hotelId);
        }
        jdbcTemplate.update("""
                update hotels
                set status = :status,
                    deleted_at = case when :status = 'ARCHIVED' then coalesce(deleted_at, now()) else deleted_at end,
                    updated_at = now()
                where id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("status", nextStatus));
        return queryHotel("select * from hotels where id = :hotelId", hotelId, user);
    }

    @Transactional
    public HotelResponse archiveHotel(UUID hotelId, Authentication authentication) {
        return changeHotelStatus(hotelId, "ARCHIVED", authentication);
    }

    public List<HotelMemberResponse> listMembers(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanViewHotel(hotelId, user);
        UUID ownerId = requireHotel(hotelId, false, user).ownerId();
        return jdbcTemplate.query("""
                select hm.hotel_id, hm.account_id, a.email, a.first_name, a.last_name, hm.created_at
                from hotel_members hm
                join accounts a on a.id = hm.account_id
                where hm.hotel_id = :hotelId
                order by a.email
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> new HotelMemberResponse(
                (UUID) rs.getObject("hotel_id"),
                (UUID) rs.getObject("account_id"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getTimestamp("created_at").toInstant(),
                ownerId.equals((UUID) rs.getObject("account_id"))
        ));
    }

    @Transactional
    public List<HotelMemberResponse> addMembers(UUID hotelId, List<UUID> accountIds, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "hotel.members.manage", hotelId);
        for (UUID accountId : distinctRequiredIds(accountIds)) {
            requireAccountExists(accountId);
            jdbcTemplate.update("""
                    insert into hotel_members (hotel_id, account_id)
                    values (:hotelId, :accountId)
                    on conflict do nothing
                    """, new MapSqlParameterSource("hotelId", hotelId).addValue("accountId", accountId));
        }
        return listMembers(hotelId, authentication);
    }

    @Transactional
    public void removeMember(UUID hotelId, UUID accountId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        HotelResponse hotel = requireHotel(hotelId, false, user);
        requireAction(user, "hotel.members.manage", hotelId);
        if (hotel.ownerId().equals(accountId)) {
            throw conflict("Cannot remove the hotel owner from hotel members");
        }
        int removed = jdbcTemplate.update("""
                delete from hotel_members
                where hotel_id = :hotelId and account_id = :accountId
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("accountId", accountId));
        if (removed == 0) {
            throw notFound("Hotel member not found");
        }
    }

    public List<AmenityResponse> listAmenities(Boolean active) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String where = "";
        if (active != null) {
            where = "where active = :active";
            params.addValue("active", active);
        }
        return jdbcTemplate.query("""
                select *
                from amenities
                """ + where + """
                order by name
                """, params, (rs, rowNum) -> mapAmenity(rs));
    }

    public AmenityResponse amenityDetail(UUID amenityId) {
        return queryAmenity("select * from amenities where id = :id", amenityId);
    }

    @Transactional
    public AmenityResponse createAmenity(AmenityRequest request, Authentication authentication) {
        requireAdmin(authentication);
        UUID id = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into amenities (id, key, name, type, active)
                    values (:id, :key, :name, :type, :active)
                    """, new MapSqlParameterSource()
                    .addValue("id", id)
                    .addValue("key", amenityKey(request.key(), request.name()))
                    .addValue("name", trimRequired(request.name(), "Amenity name is required"))
                    .addValue("type", trimRequired(request.type(), "Amenity type is required"))
                    .addValue("active", request.active() == null || request.active()));
        } catch (DuplicateKeyException exception) {
            throw badRequest("Amenity key already exists");
        }
        return amenityDetail(id);
    }

    @Transactional
    public AmenityResponse updateAmenity(UUID amenityId, AmenityRequest request, Authentication authentication) {
        requireAdmin(authentication);
        AmenityResponse current = amenityDetail(amenityId);
        try {
            jdbcTemplate.update("""
                    update amenities
                    set key = :key,
                        name = :name,
                        type = :type,
                        active = :active,
                        updated_at = now()
                    where id = :id
                    """, new MapSqlParameterSource()
                    .addValue("id", amenityId)
                    .addValue("key", request.key() == null ? current.key() : amenityKey(request.key(), request.name()))
                    .addValue("name", trimRequired(request.name(), "Amenity name is required"))
                    .addValue("type", trimRequired(request.type(), "Amenity type is required"))
                    .addValue("active", request.active() == null || request.active()));
        } catch (DuplicateKeyException exception) {
            throw badRequest("Amenity key already exists");
        }
        return amenityDetail(amenityId);
    }

    @Transactional
    public AmenityResponse disableAmenity(UUID amenityId, Authentication authentication) {
        requireAdmin(authentication);
        amenityDetail(amenityId);
        jdbcTemplate.update("""
                update amenities
                set active = false, updated_at = now()
                where id = :id
                """, new MapSqlParameterSource("id", amenityId));
        return amenityDetail(amenityId);
    }

    @Transactional
    public RoomTypeResponse createRoomType(UUID hotelId, RoomTypeRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "room_types.manage", hotelId);
        BigDecimal price = requirePrice(request.resolvedPricePerNight());
        int maxGuests = requirePositive(request.resolvedMaxGuests(), "maxGuests must be greater than 0");
        List<UUID> amenityIds = distinctRequiredIds(request.amenityIds() == null ? List.of() : request.amenityIds());
        requireActiveAmenities(amenityIds);

        UUID roomTypeId = UUID.randomUUID();
        try {
            jdbcTemplate.update("""
                    insert into room_types (
                        id, hotel_id, name, description, price_per_night, max_guests, number_of_bedrooms, active
                    ) values (
                        :id, :hotelId, :name, :description, :price, :maxGuests, :bedrooms, true
                    )
                    """, new MapSqlParameterSource()
                    .addValue("id", roomTypeId)
                    .addValue("hotelId", hotelId)
                    .addValue("name", trimRequired(request.name(), "Room type name is required"))
                    .addValue("description", trimToNull(request.description()))
                    .addValue("price", price)
                    .addValue("maxGuests", maxGuests)
                    .addValue("bedrooms", request.numberOfBedrooms() == null ? 0 : request.numberOfBedrooms()));
        } catch (DuplicateKeyException exception) {
            throw badRequest("Room type name already exists in this hotel");
        }
        replaceRoomTypeAmenities(roomTypeId, amenityIds);
        return roomTypeDetail(hotelId, roomTypeId, false);
    }

    public List<RoomTypeResponse> listRoomTypes(UUID hotelId, boolean management) {
        String hotelCondition = management ? "" : "and h.status = 'ACTIVE' and h.deleted_at is null";
        String roomTypeCondition = management ? "rt.deleted_at is null" : "rt.active and rt.deleted_at is null";
        return jdbcTemplate.query("""
                select rt.*
                from room_types rt
                join hotels h on h.id = rt.hotel_id
                where rt.hotel_id = :hotelId
                """ + hotelCondition + """
                  and """ + roomTypeCondition + """
                order by rt.price_per_night, rt.name
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapRoomType(rs, !management));
    }

    public RoomTypeResponse roomTypeDetail(UUID hotelId, UUID roomTypeId, boolean publicOnly) {
        return jdbcTemplate.query("""
                select rt.*
                from room_types rt
                join hotels h on h.id = rt.hotel_id
                where rt.id = :roomTypeId
                  and rt.hotel_id = :hotelId
                  and rt.deleted_at is null
                """ + (publicOnly ? " and rt.active and h.status = 'ACTIVE' and h.deleted_at is null" : ""),
                new MapSqlParameterSource("hotelId", hotelId).addValue("roomTypeId", roomTypeId),
                (rs, rowNum) -> mapRoomType(rs, publicOnly)
        ).stream().findFirst().orElseThrow(() -> notFound("Room type not found"));
    }

    @Transactional
    public RoomTypeResponse updateRoomType(UUID hotelId, UUID roomTypeId, RoomTypeRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "room_types.manage", hotelId);
        RoomTypeResponse current = roomTypeDetail(hotelId, roomTypeId, false);
        if (request.amenityIds() != null) {
            List<UUID> amenityIds = distinctRequiredIds(request.amenityIds());
            requireActiveAmenities(amenityIds);
            replaceRoomTypeAmenities(roomTypeId, amenityIds);
        }
        try {
            jdbcTemplate.update("""
                    update room_types
                    set name = :name,
                        description = :description,
                        price_per_night = :price,
                        max_guests = :maxGuests,
                        number_of_bedrooms = :bedrooms,
                        updated_at = now()
                    where id = :roomTypeId and hotel_id = :hotelId
                    """, new MapSqlParameterSource()
                    .addValue("hotelId", hotelId)
                    .addValue("roomTypeId", roomTypeId)
                    .addValue("name", trimOrDefault(request.name(), current.name()))
                    .addValue("description", request.description() == null ? current.description() : trimToNull(request.description()))
                    .addValue("price", request.resolvedPricePerNight() == null ? current.pricePerNight() : requirePrice(request.resolvedPricePerNight()))
                    .addValue("maxGuests", request.resolvedMaxGuests() == null ? current.maxGuests() : requirePositive(request.resolvedMaxGuests(), "maxGuests must be greater than 0"))
                    .addValue("bedrooms", request.numberOfBedrooms() == null ? current.numberOfBedrooms() : request.numberOfBedrooms()));
        } catch (DuplicateKeyException exception) {
            throw badRequest("Room type name already exists in this hotel");
        }
        return roomTypeDetail(hotelId, roomTypeId, false);
    }

    @Transactional
    public void deleteRoomType(UUID hotelId, UUID roomTypeId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "room_types.manage", hotelId);
        roomTypeDetail(hotelId, roomTypeId, false);
        long nonTerminalBookings = jdbcTemplate.queryForObject("""
                select count(*)
                from booking_items bi
                join bookings b on b.id = bi.booking_id
                where bi.room_type_id = :roomTypeId
                  and b.status in (:statuses)
                """, new MapSqlParameterSource("roomTypeId", roomTypeId).addValue("statuses", NON_TERMINAL_BOOKING_STATUSES), Long.class);
        if (nonTerminalBookings > 0) {
            throw conflict("Cannot delete a room type with non-terminal bookings");
        }
        jdbcTemplate.update("""
                update room_types
                set deleted_at = coalesce(deleted_at, now()), active = false, updated_at = now()
                where id = :roomTypeId and hotel_id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("roomTypeId", roomTypeId));
    }

    @Transactional
    public RoomResponse createRoom(UUID hotelId, RoomRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "rooms.manage", hotelId);
        if (request.roomTypeId() == null) {
            throw badRequest("roomTypeId is required");
        }
        roomTypeDetail(hotelId, request.roomTypeId(), false);
        UUID id = UUID.randomUUID();
        String condition = normalizeRoomCondition(request.condition());
        try {
            jdbcTemplate.update("""
                    insert into rooms (id, hotel_id, room_type_id, room_number, condition, active)
                    values (:id, :hotelId, :roomTypeId, :roomNumber, :condition, :active)
                    """, new MapSqlParameterSource()
                    .addValue("id", id)
                    .addValue("hotelId", hotelId)
                    .addValue("roomTypeId", request.roomTypeId())
                    .addValue("roomNumber", trimRequired(request.roomNumber(), "Room number is required"))
                    .addValue("condition", condition)
                    .addValue("active", request.active() == null || request.active()));
        } catch (DuplicateKeyException exception) {
            throw badRequest("Room number already exists in this hotel");
        }
        return roomDetail(hotelId, id);
    }

    public List<RoomResponse> listRooms(UUID hotelId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanViewHotel(hotelId, user);
        return jdbcTemplate.query("""
                select *
                from rooms
                where hotel_id = :hotelId
                order by room_number
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> mapRoom(rs));
    }

    @Transactional
    public RoomResponse updateRoom(UUID hotelId, UUID roomId, RoomRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "rooms.manage", hotelId);
        RoomResponse current = roomDetail(hotelId, roomId);
        UUID roomTypeId = request.roomTypeId() == null ? current.roomTypeId() : request.roomTypeId();
        roomTypeDetail(hotelId, roomTypeId, false);
        try {
            jdbcTemplate.update("""
                    update rooms
                    set room_type_id = :roomTypeId,
                        room_number = :roomNumber,
                        condition = :condition,
                        active = :active,
                        updated_at = now()
                    where id = :roomId and hotel_id = :hotelId
                    """, new MapSqlParameterSource()
                    .addValue("hotelId", hotelId)
                    .addValue("roomId", roomId)
                    .addValue("roomTypeId", roomTypeId)
                    .addValue("roomNumber", trimOrDefault(request.roomNumber(), current.roomNumber()))
                    .addValue("condition", request.condition() == null ? current.condition() : normalizeRoomCondition(request.condition()))
                    .addValue("active", request.active() == null ? current.active() : request.active()));
        } catch (DuplicateKeyException exception) {
            throw badRequest("Room number already exists in this hotel");
        }
        return roomDetail(hotelId, roomId);
    }

    @Transactional
    public void deleteRoom(UUID hotelId, UUID roomId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "rooms.manage", hotelId);
        int deleted = jdbcTemplate.update("""
                delete from rooms
                where id = :roomId and hotel_id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("roomId", roomId));
        if (deleted == 0) {
            throw notFound("Room not found");
        }
    }

    @Transactional
    public InventoryResponse upsertInventory(UUID hotelId, UUID roomTypeId, InventoryRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user, "inventory.manage", hotelId);
        roomTypeDetail(hotelId, roomTypeId, false);
        if (request.availableRooms() > request.totalRooms()) {
            throw badRequest("availableRooms cannot exceed totalRooms");
        }
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms, stop_sell)
                values (:id, :hotelId, :roomTypeId, :date, :totalRooms, :availableRooms, :stopSell)
                on conflict (room_type_id, stay_date)
                do update set total_rooms = excluded.total_rooms,
                              available_rooms = excluded.available_rooms,
                              stop_sell = excluded.stop_sell,
                              updated_at = now()
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId)
                .addValue("date", request.date())
                .addValue("totalRooms", request.totalRooms())
                .addValue("availableRooms", request.availableRooms())
                .addValue("stopSell", request.stopSell() != null && request.stopSell()));
        return inventoryDetail(hotelId, roomTypeId, request.date());
    }

    public List<InventoryResponse> listInventory(UUID hotelId, UUID roomTypeId, LocalDate from, LocalDate to, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireCanViewHotel(hotelId, user);
        roomTypeDetail(hotelId, roomTypeId, false);
        return jdbcTemplate.query("""
                select *
                from inventories
                where hotel_id = :hotelId
                  and room_type_id = :roomTypeId
                  and (:fromDate is null or stay_date >= :fromDate)
                  and (:toDate is null or stay_date < :toDate)
                order by stay_date
                """, new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId)
                .addValue("fromDate", from)
                .addValue("toDate", to), (rs, rowNum) -> mapInventory(rs));
    }

    public PaginatedResponse<AvailabilityResponse> publicAvailability(UUID hotelId, LocalDate from, LocalDate to, int limit, int offset) {
        if (from == null || to == null || !to.isAfter(from)) {
            throw badRequest("from must be before checkout-exclusive to");
        }
        long nights = ChronoUnit.DAYS.between(from, to);
        int boundedLimit = boundedLimit(limit);
        int boundedOffset = Math.max(offset, 0);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("fromDate", from)
                .addValue("toDate", to)
                .addValue("nights", nights)
                .addValue("limit", boundedLimit)
                .addValue("offset", boundedOffset);
        String availabilityBase = """
                from room_types rt
                join hotels h on h.id = rt.hotel_id
                join inventories i on i.room_type_id = rt.id
                where rt.hotel_id = :hotelId
                  and h.status = 'ACTIVE'
                  and h.deleted_at is null
                  and rt.active
                  and rt.deleted_at is null
                  and i.stay_date >= :fromDate
                  and i.stay_date < :toDate
                  and not i.stop_sell
                  and i.available_rooms > 0
                group by rt.id, rt.hotel_id, rt.name, rt.description, rt.price_per_night, rt.max_guests,
                         rt.number_of_bedrooms
                having count(distinct i.stay_date) = :nights
                """;
        long total = jdbcTemplate.queryForObject("""
                select count(*)
                from (
                    select rt.id
                    """ + availabilityBase + """
                ) available_room_types
                """, params, Long.class);
        List<AvailabilityResponse> roomTypes = jdbcTemplate.query("""
                select rt.id, rt.hotel_id, rt.name, rt.description, rt.price_per_night, rt.max_guests,
                       coalesce(rt.number_of_bedrooms, 0) as number_of_bedrooms,
                       min(i.available_rooms) as available_rooms
                """ + availabilityBase + """
                order by rt.price_per_night asc, rt.name asc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> new AvailabilityResponse(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("hotel_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price_per_night"),
                rs.getInt("max_guests"),
                rs.getInt("number_of_bedrooms"),
                rs.getInt("available_rooms"),
                loadAmenities((UUID) rs.getObject("id"), true)
        ));
        return new PaginatedResponse<>(roomTypes, new PageMeta(boundedLimit, boundedOffset, total));
    }

    private HotelResponse requireHotel(UUID hotelId, boolean includeArchived, CurrentUser user) {
        String archivalPredicate = includeArchived ? "" : " and h.deleted_at is null and h.status in ('DRAFT', 'ACTIVE', 'SUSPENDED')";
        return queryHotel("select h.* from hotels h where h.id = :hotelId" + archivalPredicate, hotelId, user);
    }

    private HotelResponse queryHotel(String sql, UUID hotelId, CurrentUser user) {
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource("hotelId", hotelId),
                (rs, rowNum) -> mapHotel(rs, user)
        ).stream().findFirst().orElseThrow(() -> notFound("Hotel not found"));
    }

    private HotelResponse mapHotel(ResultSet rs, CurrentUser user) throws SQLException {
        UUID hotelId = (UUID) rs.getObject("id");
        UUID ownerId = (UUID) rs.getObject("owner_id");
        return new HotelResponse(
                hotelId,
                ownerId,
                rs.getString("name"),
                rs.getString("slug"),
                rs.getString("description"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("country"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getBigDecimal("star_rating"),
                rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant(),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                user == null ? null : allowedActions(user, hotelId)
        );
    }

    private List<String> allowedActions(CurrentUser user, UUID hotelId) {
        return jdbcTemplate.query("""
                select distinct a.key
                from api_actions a
                join action_policies ap on ap.action_id = a.id
                join role_permissions rp on rp.permission_id = ap.permission_id
                join account_roles ar on ar.role_id = rp.role_id
                where a.enabled
                  and ar.account_id = :accountId
                  and (
                      ap.scope = 'GLOBAL'
                      or (
                          ap.scope = 'HOTEL_MEMBER'
                          and exists (
                              select 1
                              from hotel_members hm
                              where hm.hotel_id = :hotelId
                                and hm.account_id = :accountId
                          )
                      )
                      or (
                          ap.scope = 'HOTEL_OWNER'
                          and exists (
                              select 1
                              from hotels h
                              where h.id = :hotelId
                                and h.owner_id = :accountId
                          )
                      )
                  )
                order by a.key
                """, new MapSqlParameterSource("accountId", user.accountId()).addValue("hotelId", hotelId),
                (rs, rowNum) -> rs.getString("key"));
    }

    private AmenityResponse queryAmenity(String sql, UUID amenityId) {
        return jdbcTemplate.query(sql,
                new MapSqlParameterSource("id", amenityId),
                (rs, rowNum) -> mapAmenity(rs)
        ).stream().findFirst().orElseThrow(() -> notFound("Amenity not found"));
    }

    private AmenityResponse mapAmenity(ResultSet rs) throws SQLException {
        return new AmenityResponse(
                (UUID) rs.getObject("id"),
                rs.getString("key"),
                rs.getString("name"),
                rs.getString("type"),
                rs.getBoolean("active"),
                rs.getBoolean("is_system"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private RoomTypeResponse mapRoomType(ResultSet rs, boolean publicOnly) throws SQLException {
        UUID roomTypeId = (UUID) rs.getObject("id");
        return new RoomTypeResponse(
                roomTypeId,
                (UUID) rs.getObject("hotel_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price_per_night"),
                rs.getInt("max_guests"),
                rs.getObject("number_of_bedrooms") == null ? 0 : rs.getInt("number_of_bedrooms"),
                rs.getBoolean("active"),
                rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant(),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                loadAmenities(roomTypeId, publicOnly)
        );
    }

    private List<AmenityResponse> loadAmenities(UUID roomTypeId, boolean publicOnly) {
        return jdbcTemplate.query("""
                select a.*
                from room_type_amenities rta
                join amenities a on a.id = rta.amenity_id
                where rta.room_type_id = :roomTypeId
                """ + (publicOnly ? " and a.active\n" : "") + """
                order by a.name
                """, new MapSqlParameterSource("roomTypeId", roomTypeId), (rs, rowNum) -> mapAmenity(rs));
    }

    private RoomResponse roomDetail(UUID hotelId, UUID roomId) {
        return jdbcTemplate.query("""
                select *
                from rooms
                where id = :roomId and hotel_id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("roomId", roomId),
                (rs, rowNum) -> mapRoom(rs)
        ).stream().findFirst().orElseThrow(() -> notFound("Room not found"));
    }

    private RoomResponse mapRoom(ResultSet rs) throws SQLException {
        return new RoomResponse(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("hotel_id"),
                (UUID) rs.getObject("room_type_id"),
                rs.getString("room_number"),
                rs.getString("condition"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private InventoryResponse inventoryDetail(UUID hotelId, UUID roomTypeId, LocalDate date) {
        return jdbcTemplate.query("""
                select *
                from inventories
                where hotel_id = :hotelId and room_type_id = :roomTypeId and stay_date = :date
                """, new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId)
                .addValue("date", date), (rs, rowNum) -> mapInventory(rs)
        ).stream().findFirst().orElseThrow(() -> notFound("Inventory not found"));
    }

    private InventoryResponse mapInventory(ResultSet rs) throws SQLException {
        return new InventoryResponse(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("hotel_id"),
                (UUID) rs.getObject("room_type_id"),
                rs.getObject("stay_date", LocalDate.class),
                rs.getInt("total_rooms"),
                rs.getInt("available_rooms"),
                rs.getBoolean("stop_sell"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private void requireCanViewHotel(UUID hotelId, CurrentUser user) {
        HotelResponse hotel = queryHotel("select * from hotels where id = :hotelId", hotelId, user);
        if (hotel.deletedAt() != null || "ARCHIVED".equals(hotel.status())) {
            throw notFound("Hotel not found");
        }
        if (hotel.ownerId().equals(user.accountId())) {
            return;
        }
        requireAction(user, "hotels.manage", hotelId);
    }

    private void requireAction(CurrentUser user, String actionKey, UUID hotelId) {
        if (!hasAction(user, actionKey, hotelId)) {
            throw forbidden("Action not allowed: " + actionKey);
        }
    }

    private boolean hasAction(CurrentUser user, String actionKey, UUID hotelId) {
        Boolean allowed = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from api_actions a
                    join action_policies ap on ap.action_id = a.id
                    join role_permissions rp on rp.permission_id = ap.permission_id
                    join account_roles ar on ar.role_id = rp.role_id
                    where a.key = :actionKey
                      and a.enabled
                      and ar.account_id = :accountId
                      and (
                          ap.scope = 'GLOBAL'
                          or (
                              cast(:hotelId as uuid) is not null
                              and ap.scope = 'HOTEL_MEMBER'
                              and exists (
                                  select 1
                                  from hotel_members hm
                                  where hm.hotel_id = :hotelId
                                    and hm.account_id = :accountId
                              )
                          )
                          or (
                              cast(:hotelId as uuid) is not null
                              and ap.scope = 'HOTEL_OWNER'
                              and exists (
                                  select 1
                                  from hotels h
                                  where h.id = :hotelId
                                    and h.owner_id = :accountId
                              )
                          )
                      )
                )
                """, new MapSqlParameterSource("actionKey", actionKey)
                .addValue("accountId", user.accountId())
                .addValue("hotelId", hotelId, Types.OTHER), Boolean.class);
        return Boolean.TRUE.equals(allowed);
    }

    private boolean hasPermission(CurrentUser user, String permissionKey) {
        Boolean allowed = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from account_roles ar
                    join role_permissions rp on rp.role_id = ar.role_id
                    join permissions p on p.id = rp.permission_id
                    where ar.account_id = :accountId
                      and p.key = :permissionKey
                )
                """, new MapSqlParameterSource("accountId", user.accountId()).addValue("permissionKey", permissionKey), Boolean.class);
        return Boolean.TRUE.equals(allowed);
    }

    private void requireAccountExists(UUID accountId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (select 1 from accounts where id = :accountId)
                """, new MapSqlParameterSource("accountId", accountId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw notFound("Account not found");
        }
    }

    private void requireActiveAmenities(List<UUID> amenityIds) {
        if (amenityIds.isEmpty()) {
            return;
        }
        long count = jdbcTemplate.queryForObject("""
                select count(*)
                from amenities
                where id in (:ids) and active
                """, new MapSqlParameterSource("ids", amenityIds), Long.class);
        if (count != amenityIds.size()) {
            throw badRequest("Amenity IDs must reference active amenities");
        }
    }

    private void replaceRoomTypeAmenities(UUID roomTypeId, List<UUID> amenityIds) {
        jdbcTemplate.update("delete from room_type_amenities where room_type_id = :roomTypeId",
                new MapSqlParameterSource("roomTypeId", roomTypeId));
        for (UUID amenityId : amenityIds) {
            jdbcTemplate.update("""
                    insert into room_type_amenities (room_type_id, amenity_id)
                    values (:roomTypeId, :amenityId)
                    """, new MapSqlParameterSource("roomTypeId", roomTypeId).addValue("amenityId", amenityId));
        }
    }

    private void validateHotelStatusTransition(String currentStatus, String nextStatus) {
        if (!HOTEL_STATUSES.contains(nextStatus)) {
            throw badRequest("Invalid hotel status");
        }
        if (currentStatus.equals(nextStatus)) {
            return;
        }
        boolean allowed = switch (currentStatus) {
            case "DRAFT" -> "ACTIVE".equals(nextStatus) || "ARCHIVED".equals(nextStatus);
            case "ACTIVE" -> "SUSPENDED".equals(nextStatus) || "ARCHIVED".equals(nextStatus);
            case "SUSPENDED" -> "ACTIVE".equals(nextStatus) || "ARCHIVED".equals(nextStatus);
            default -> false;
        };
        if (!allowed) {
            throw conflict("Invalid hotel status transition");
        }
    }

    private void rejectArchiveWithNonTerminalBookings(UUID hotelId) {
        long count = jdbcTemplate.queryForObject("""
                select count(*)
                from bookings
                where hotel_id = :hotelId and status in (:statuses)
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("statuses", NON_TERMINAL_BOOKING_STATUSES), Long.class);
        if (count > 0) {
            throw conflict("Cannot archive a hotel with non-terminal bookings");
        }
    }

    private String uniqueHotelSlug(String name) {
        String base = slugify(name);
        String slug = base;
        int suffix = 2;
        while (slugExists(slug)) {
            slug = base + "-" + suffix;
            suffix++;
        }
        return slug;
    }

    private boolean slugExists(String slug) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (select 1 from hotels where slug = :slug)
                """, new MapSqlParameterSource("slug", slug), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private CurrentUser requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountAuthUser account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return new CurrentUser(account.getAccountId());
    }

    private void requireAdmin(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (!hasPermission(user, "security.manage")) {
            throw forbidden("ADMIN role required");
        }
    }

    private List<UUID> distinctRequiredIds(List<UUID> ids) {
        if (ids == null) {
            throw badRequest("IDs are required");
        }
        Set<UUID> unique = new HashSet<>();
        for (UUID id : ids) {
            if (id == null || !unique.add(id)) {
                throw badRequest("IDs must be non-null and unique");
            }
        }
        return List.copyOf(unique);
    }

    private String normalizeStatus(String status) {
        return trimRequired(status, "Hotel status is required").toUpperCase(Locale.ROOT);
    }

    private String normalizeRoomCondition(String condition) {
        String normalized = condition == null ? "CLEAN" : trimRequired(condition, "Room condition is required").toUpperCase(Locale.ROOT);
        if (!ROOM_CONDITIONS.contains(normalized)) {
            throw badRequest("Invalid room condition");
        }
        return normalized;
    }

    private BigDecimal requirePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw badRequest("pricePerNight must be zero or greater");
        }
        return price;
    }

    private int requirePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw badRequest(message);
        }
        return value;
    }

    private int boundedLimit(int limit) {
        if (limit <= 0) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private String trimRequired(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw badRequest(message);
        }
        return trimmed;
    }

    private String trimOrDefault(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String slugify(String value) {
        String slug = trimRequired(value, "Hotel name is required")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "hotel" : slug;
    }

    private String amenityKey(String key, String name) {
        String keySource = key == null ? name : key;
        String normalized = trimRequired(keySource, "Amenity key is required")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("(^_|_$)", "");
        if (normalized.isBlank()) {
            throw badRequest("Amenity key is required");
        }
        return normalized;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private record CurrentUser(UUID accountId) {
    }
}
