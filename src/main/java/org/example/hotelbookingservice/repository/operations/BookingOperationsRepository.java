package org.example.hotelbookingservice.repository.operations;

import org.example.hotelbookingservice.dto.response.booking.operations.BookingGuestResponse;
import org.example.hotelbookingservice.dto.response.booking.operations.CheckInSummary;
import org.example.hotelbookingservice.entity.CheckIn;
import org.example.hotelbookingservice.entity.GuestDetail;
import org.example.hotelbookingservice.entity.Promotion;
import org.example.hotelbookingservice.repository.CheckInRepository;
import org.example.hotelbookingservice.repository.CommissionPackageRepository;
import org.example.hotelbookingservice.repository.GuestDetailRepository;
import org.example.hotelbookingservice.repository.InventoryRepository;
import org.example.hotelbookingservice.repository.PaymentEventRepository;
import org.example.hotelbookingservice.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookingOperationsRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final InventoryRepository inventoryRepository;
    private final CheckInRepository checkInRepository;
    private final GuestDetailRepository guestDetailRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final PromotionRepository promotionRepository;
    private final CommissionPackageRepository commissionPackageRepository;

    public BookingOperationsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, null, null, null, null, null, null);
    }

    @Autowired
    public BookingOperationsRepository(NamedParameterJdbcTemplate jdbcTemplate,
                                         InventoryRepository inventoryRepository,
                                         CheckInRepository checkInRepository,
                                         GuestDetailRepository guestDetailRepository,
                                         PaymentEventRepository paymentEventRepository,
                                         PromotionRepository promotionRepository,
                                         CommissionPackageRepository commissionPackageRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.inventoryRepository = inventoryRepository;
        this.checkInRepository = checkInRepository;
        this.guestDetailRepository = guestDetailRepository;
        this.paymentEventRepository = paymentEventRepository;
        this.promotionRepository = promotionRepository;
        this.commissionPackageRepository = commissionPackageRepository;
    }

    public int update(String sql, MapSqlParameterSource params) {
        return jdbcTemplate.update(sql, params);
    }

    public <T> T queryForObject(String sql, MapSqlParameterSource params, Class<T> requiredType) {
        return jdbcTemplate.queryForObject(sql, params, requiredType);
    }

    public <T> List<T> query(String sql, MapSqlParameterSource params, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    public int reserveInventory(UUID hotelId, UUID roomTypeId, java.time.LocalDate stayDate, int quantity) {
        if (inventoryRepository != null) {
            return inventoryRepository.reserveAvailableRooms(hotelId, roomTypeId, stayDate, quantity);
        }
        return update("""
                update inventories
                set available_rooms = available_rooms - :quantity,
                    updated_at = now()
                where hotel_id = :hotelId
                  and room_type_id = :roomTypeId
                  and stay_date = :stayDate
                  and not stop_sell
                  and available_rooms >= :quantity
                """, new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("roomTypeId", roomTypeId)
                .addValue("stayDate", stayDate)
                .addValue("quantity", quantity));
    }

    public int releaseInventory(UUID roomTypeId, java.time.LocalDate stayDate, int quantity) {
        if (inventoryRepository != null) {
            return inventoryRepository.releaseAvailableRooms(roomTypeId, stayDate, quantity);
        }
        return update("""
                update inventories
                set available_rooms = least(total_rooms, available_rooms + :quantity),
                    updated_at = now()
                where room_type_id = :roomTypeId
                  and stay_date = :stayDate
                """, new MapSqlParameterSource()
                .addValue("roomTypeId", roomTypeId)
                .addValue("stayDate", stayDate)
                .addValue("quantity", quantity));
    }

    public UUID upsertCheckIn(UUID bookingId, UUID checkedInBy, String note) {
        UUID existing = existingCheckInId(bookingId);
        if (existing != null) {
            updateCheckInNote(existing, note);
            deleteGuests(existing);
            return existing;
        }
        UUID id = UUID.randomUUID();
        if (checkInRepository != null) {
            checkInRepository.insertCheckIn(id, bookingId, checkedInBy, note);
            return id;
        }
        update("""
                insert into check_ins (id, booking_id, checked_in_by_account_id, checked_in_at, note)
                values (:id, :bookingId, :checkedInBy, now(), :note)
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("bookingId", bookingId)
                .addValue("checkedInBy", checkedInBy)
                .addValue("note", note));
        return id;
    }

    public UUID existingCheckInId(UUID bookingId) {
        if (checkInRepository != null) {
            return checkInRepository.findByBooking_Id(bookingId).map(CheckIn::getId).orElse(null);
        }
        return query("""
                select id
                from check_ins
                where booking_id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> (UUID) rs.getObject("id"))
                .stream().findFirst().orElse(null);
    }

    public void updateCheckInNote(UUID checkInId, String note) {
        if (checkInRepository != null) {
            checkInRepository.updateNote(checkInId, note);
            return;
        }
        update("""
                update check_ins
                set note = :note
                where id = :id
                """, new MapSqlParameterSource("id", checkInId).addValue("note", note));
    }

    public void markCheckedOut(UUID checkInId) {
        if (checkInRepository != null) {
            checkInRepository.markCheckedOut(checkInId);
            return;
        }
        update("""
                update check_ins
                set checked_out_at = coalesce(checked_out_at, now())
                where id = :checkInId
                """, new MapSqlParameterSource("checkInId", checkInId));
    }

    public CheckInSummary loadCheckInSummary(UUID bookingId) {
        if (checkInRepository != null && guestDetailRepository != null) {
            Optional<CheckIn> checkIn = checkInRepository.findByBooking_Id(bookingId);
            return checkIn.map(value -> new CheckInSummary(
                    value.getId(),
                    value.getBookingId(),
                    value.getCheckedInByAccountId(),
                    value.getCheckedInAt(),
                    value.getCheckedOutAt(),
                    value.getNote(),
                    (int) guestDetailRepository.countByCheckIn_Id(value.getId())
            )).orElse(null);
        }
        return query("""
                select ci.*,
                       (select count(*) from booking_guests bg where bg.check_in_id = ci.id) as guest_count
                from check_ins ci
                where ci.booking_id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> new CheckInSummary(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("booking_id"),
                (UUID) rs.getObject("checked_in_by_account_id"),
                toInstant(rs.getTimestamp("checked_in_at")),
                toInstant(rs.getTimestamp("checked_out_at")),
                rs.getString("note"),
                rs.getInt("guest_count")
        )).stream().findFirst().orElse(null);
    }

    public List<BookingGuestResponse> loadGuests(UUID checkInId, UUID bookingId) {
        if (guestDetailRepository != null) {
            return guestDetailRepository.findByCheckIn_IdOrderByGuestOrder(checkInId).stream()
                    .map(guest -> new BookingGuestResponse(
                            guest.getUuid(),
                            bookingId,
                            checkInId,
                            guest.getFullName(),
                            guest.getIdentityNumber(),
                            guest.getPhone(),
                            Boolean.TRUE.equals(guest.getPrimaryGuest()),
                            guest.getGuestOrder()
                    ))
                    .toList();
        }
        return query("""
                select *
                from booking_guests
                where check_in_id = :checkInId
                order by guest_order
                """, new MapSqlParameterSource("checkInId", checkInId), (rs, rowNum) -> new BookingGuestResponse(
                (UUID) rs.getObject("id"),
                bookingId,
                (UUID) rs.getObject("check_in_id"),
                rs.getString("full_name"),
                rs.getString("identity_number"),
                rs.getString("phone"),
                rs.getBoolean("is_primary"),
                rs.getInt("guest_order")
        ));
    }

    public void deleteGuests(UUID checkInId) {
        if (guestDetailRepository != null) {
            guestDetailRepository.deleteByCheckInId(checkInId);
            return;
        }
        update("delete from booking_guests where check_in_id = :checkInId",
                new MapSqlParameterSource("checkInId", checkInId));
    }

    public void insertGuest(UUID checkInId,
                            String fullName,
                            String identityNumber,
                            String phone,
                            boolean primary,
                            int guestOrder) {
        UUID id = UUID.randomUUID();
        if (guestDetailRepository != null) {
            guestDetailRepository.insertGuest(id, checkInId, fullName, identityNumber, phone, primary, guestOrder);
            return;
        }
        update("""
                insert into booking_guests (id, check_in_id, full_name, identity_number, phone, is_primary, guest_order)
                values (:id, :checkInId, :fullName, :identityNumber, :phone, :primary, :guestOrder)
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("checkInId", checkInId)
                .addValue("fullName", fullName)
                .addValue("identityNumber", identityNumber)
                .addValue("phone", phone)
                  .addValue("primary", primary)
                  .addValue("guestOrder", guestOrder));
    }

    public void insertPaymentEvent(UUID eventId, UUID paymentId, String eventType, String payloadJson) {
        if (paymentEventRepository != null) {
            paymentEventRepository.insertEvent(eventId, paymentId, eventType, payloadJson);
            return;
        }
        update("""
                insert into payment_events (id, payment_id, event_type, payload)
                values (:id, :paymentId, :eventType, cast(:payload as jsonb))
                """, new MapSqlParameterSource()
                .addValue("id", eventId)
                .addValue("paymentId", paymentId)
                .addValue("eventType", eventType)
                .addValue("payload", payloadJson));
    }

    public Optional<Promotion> findActivePromotionForBooking(UUID hotelId, String code, BigDecimal subtotal, Instant now) {
        if (promotionRepository == null) {
            return Optional.empty();
        }
        return promotionRepository.findActiveForBooking(hotelId, code, subtotal, now);
    }

    public long countPromotionUsesByAccount(UUID accountId, UUID promotionId) {
        if (promotionRepository != null) {
            return promotionRepository.countNonCancelledUsesByAccount(accountId, promotionId);
        }
        return queryForObject("""
                select count(*)
                from bookings
                where account_id = :accountId
                  and promotion_id = :promotionId
                  and status <> 'CANCELLED'
                """, new MapSqlParameterSource("accountId", accountId).addValue("promotionId", promotionId), Long.class);
    }

    public int incrementPromotionUsedCount(UUID promotionId, Instant now) {
        if (promotionRepository != null) {
            return promotionRepository.incrementUsedCount(promotionId, now);
        }
        return update("update promotions set used_count = used_count + 1, updated_at = now() where id = :promotionId",
                new MapSqlParameterSource("promotionId", promotionId));
    }

    public int decrementPromotionUsedCount(UUID promotionId, Instant now) {
        if (promotionRepository != null) {
            return promotionRepository.decrementUsedCount(promotionId, now);
        }
        return update("""
                update promotions
                set used_count = greatest(0, used_count - 1), updated_at = now()
                where id = :promotionId
                """, new MapSqlParameterSource("promotionId", promotionId));
    }

    public Optional<CommissionPackageRepository.CommissionRateView> findCommissionForHotel(UUID hotelId) {
        if (commissionPackageRepository == null) {
            return Optional.empty();
        }
        return commissionPackageRepository.findCommissionForHotel(hotelId);
    }

    private static Instant toInstant(java.sql.Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
