package org.example.hotelbookingservice.services;

import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingCreateRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingGuestResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingItemRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingItemResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.BookingResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CheckInDetailResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CheckInGuestRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CheckInRequest;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CheckInSummary;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.CommissionSummary;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.HotelSummary;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.PaymentStartResponse;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.PaymentSummary;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.PromotionSummary;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.RoomTypeSummary;
import org.example.hotelbookingservice.dto.operations.BookingOperationsDtos.UserSummary;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PageMeta;
import org.example.hotelbookingservice.dto.operations.HotelOperationsDtos.PaginatedResponse;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingOperationsService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int PENDING_EXPIRY_MINUTES = 15;
    private static final Set<String> BOOKING_STATUSES = Set.of("PENDING", "CONFIRMED", "CHECKED_IN", "COMPLETED", "CANCELLED", "NO_SHOW");
    private static final Set<String> ACTIVE_PAYMENT_STATUSES = Set.of("INIT", "PENDING");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Clock clock;

    @Transactional
    public BookingResponse createBooking(UUID hotelId, BookingCreateRequest request, Authentication authentication) {
        CurrentUser user = requireVerifiedUser(authentication);
        requirePermission(user.accountId(), "bookings.create");
        requireActiveHotel(hotelId);
        validateCreateRequest(request);

        List<BookingItemRequest> items = distinctItems(request.items());
        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        List<RoomTypeForBooking> roomTypes = loadRoomTypes(hotelId, items);
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BookingLine> lines = new ArrayList<>();
        for (BookingItemRequest item : items) {
            RoomTypeForBooking roomType = roomTypes.stream()
                    .filter(candidate -> candidate.id().equals(item.roomTypeId()))
                    .findFirst()
                    .orElseThrow(() -> badRequest("Room type IDs must belong to this hotel"));
            BigDecimal lineTotal = roomType.pricePerNight()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .multiply(BigDecimal.valueOf(nights))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);
            lines.add(new BookingLine(UUID.randomUUID(), roomType, item.quantity(), lineTotal));
        }
        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Booking subtotal must be greater than zero");
        }

        PromotionForBooking promotion = resolvePromotion(hotelId, user.accountId(), request.promotionCode(), subtotal);
        BigDecimal discount = promotion == null ? BigDecimal.ZERO : promotion.discountAmount();
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        CommissionSnapshot commission = resolveCommission(hotelId, total);

        reserveInventory(hotelId, request.checkIn(), request.checkOut(), items);
        if (promotion != null) {
            consumePromotion(promotion.id());
        }

        UUID bookingId = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into bookings (
                    id, account_id, hotel_id, promotion_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, note, subtotal_amount, discount_amount, total_amount,
                    commission_package_code, commission_rate, commission_amount, pending_expires_at
                ) values (
                    :id, :accountId, :hotelId, :promotionId, :reference, 'PENDING', :checkIn, :checkOut,
                    :guestName, :guestEmail, :guestPhone, :note, :subtotal, :discount, :total,
                    :commissionPackageCode, :commissionRate, :commissionAmount, :pendingExpiresAt
                )
                """, new MapSqlParameterSource()
                .addValue("id", bookingId)
                .addValue("accountId", user.accountId())
                .addValue("hotelId", hotelId)
                .addValue("promotionId", promotion == null ? null : promotion.id(), Types.OTHER)
                .addValue("reference", "BK-" + bookingId)
                .addValue("checkIn", request.checkIn())
                .addValue("checkOut", request.checkOut())
                .addValue("guestName", trimRequired(request.guestName(), "guestName is required"))
                .addValue("guestEmail", trimRequired(request.guestEmail(), "guestEmail is required"))
                .addValue("guestPhone", trimRequired(request.guestPhone(), "guestPhone is required"))
                .addValue("note", trimToNull(request.note()))
                .addValue("subtotal", subtotal)
                .addValue("discount", discount)
                .addValue("total", total)
                .addValue("commissionPackageCode", commission.packageCode())
                .addValue("commissionRate", commission.rate())
                .addValue("commissionAmount", commission.amount())
                .addValue("pendingExpiresAt", timestamp(now().plus(PENDING_EXPIRY_MINUTES, ChronoUnit.MINUTES))));

        for (BookingLine line : lines) {
            jdbcTemplate.update("""
                    insert into booking_items (
                        id, booking_id, room_type_id, room_type_name, quantity, unit_price, max_guests, line_total
                    ) values (
                        :id, :bookingId, :roomTypeId, :roomTypeName, :quantity, :unitPrice, :maxGuests, :lineTotal
                    )
                    """, new MapSqlParameterSource()
                    .addValue("id", line.id())
                    .addValue("bookingId", bookingId)
                    .addValue("roomTypeId", line.roomType().id())
                    .addValue("roomTypeName", line.roomType().name())
                    .addValue("quantity", line.quantity())
                    .addValue("unitPrice", line.roomType().pricePerNight())
                    .addValue("maxGuests", line.roomType().maxGuests())
                    .addValue("lineTotal", line.lineTotal()));
        }

        return bookingDetail(bookingId, Audience.CUSTOMER, user.accountId());
    }

    @Transactional
    public PaginatedResponse<BookingResponse> listMine(int limit, int offset, String status, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        expireDuePendingBookings();
        int boundedLimit = boundedLimit(limit);
        int boundedOffset = Math.max(offset, 0);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountId", user.accountId())
                .addValue("status", normalizeOptionalStatus(status))
                .addValue("limit", boundedLimit)
                .addValue("offset", boundedOffset);
        String where = """
                from bookings b
                where b.account_id = :accountId
                  and (:status is null or b.status = :status)
                """;
        long total = jdbcTemplate.queryForObject("select count(*) " + where, params, Long.class);
        List<BookingResponse> rows = jdbcTemplate.query("""
                select b.*
                """ + where + """
                order by b.created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapBooking(rs, Audience.CUSTOMER));
        return new PaginatedResponse<>(rows, new PageMeta(boundedLimit, boundedOffset, total));
    }

    @Transactional
    public BookingResponse mineDetail(UUID bookingId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        expireBookingIfDue(bookingId);
        return bookingDetail(bookingId, Audience.CUSTOMER, user.accountId());
    }

    @Transactional
    public PaginatedResponse<BookingResponse> listHotelBookings(UUID hotelId, int limit, int offset, String status, String q, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user.accountId(), "bookings.list.hotel", hotelId);
        expireDuePendingBookings();
        int boundedLimit = boundedLimit(limit);
        int boundedOffset = Math.max(offset, 0);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("hotelId", hotelId)
                .addValue("status", normalizeOptionalStatus(status))
                .addValue("q", likeQuery(q))
                .addValue("limit", boundedLimit)
                .addValue("offset", boundedOffset);
        String where = """
                from bookings b
                where b.hotel_id = :hotelId
                  and (:status is null or b.status = :status)
                  and (:q is null or lower(b.guest_name) like :q or lower(b.guest_email) like :q or lower(b.guest_phone) like :q)
                """;
        long total = jdbcTemplate.queryForObject("select count(*) " + where, params, Long.class);
        List<BookingResponse> rows = jdbcTemplate.query("""
                select b.*
                """ + where + """
                order by b.created_at desc
                limit :limit offset :offset
                """, params, (rs, rowNum) -> mapBooking(rs, Audience.OPERATIONS));
        return new PaginatedResponse<>(rows, new PageMeta(boundedLimit, boundedOffset, total));
    }

    @Transactional
    public BookingResponse hotelBookingDetail(UUID hotelId, UUID bookingId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user.accountId(), "bookings.list.hotel", hotelId);
        expireBookingIfDue(bookingId);
        return scopedHotelBookingDetail(hotelId, bookingId);
    }

    @Transactional
    public BookingResponse cancelMine(UUID bookingId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        expireBookingIfDue(bookingId);
        bookingDetail(bookingId, Audience.CUSTOMER, user.accountId());
        BookingForUpdate booking = lockBooking(bookingId);
        if (!booking.accountId().equals(user.accountId())) {
            throw notFound("Booking not found");
        }
        if (!"PENDING".equals(booking.status()) || hasSuccessfulPayment(bookingId)) {
            throw badRequest("Only unpaid pending bookings can be cancelled by the customer");
        }
        transitionToCancelled(booking, true);
        return bookingDetail(bookingId, Audience.CUSTOMER, user.accountId());
    }

    @Transactional
    public BookingResponse cancelHotelBooking(UUID hotelId, UUID bookingId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user.accountId(), "bookings.status.update", hotelId);
        BookingForUpdate booking = lockHotelBooking(hotelId, bookingId);
        transitionToCancelled(booking, !hasSuccessfulPayment(bookingId));
        return scopedHotelBookingDetail(hotelId, bookingId);
    }

    @Transactional
    public BookingResponse updateStatus(UUID hotelId, UUID bookingId, String requestedStatus, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user.accountId(), "bookings.status.update", hotelId);
        BookingForUpdate booking = lockHotelBooking(hotelId, bookingId);
        String nextStatus = normalizeStatus(requestedStatus);
        switch (nextStatus) {
            case "CANCELLED" -> transitionToCancelled(booking, !hasSuccessfulPayment(bookingId));
            case "COMPLETED" -> completeCheckout(booking);
            case "NO_SHOW" -> markNoShow(booking);
            case "CHECKED_IN" -> throw badRequest("Use the check-in endpoint to check in a booking");
            case "CONFIRMED" -> throw badRequest("Payment confirmation is required to confirm bookings");
            case "PENDING" -> throw badRequest("Cannot move a booking back to pending");
            default -> throw badRequest("Invalid booking status");
        }
        return scopedHotelBookingDetail(hotelId, bookingId);
    }

    @Transactional
    public BookingResponse checkIn(UUID hotelId, UUID bookingId, CheckInRequest request, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user.accountId(), "bookings.check_in", hotelId);
        BookingForUpdate booking = lockHotelBooking(hotelId, bookingId);
        if (!"CONFIRMED".equals(booking.status()) && !"CHECKED_IN".equals(booking.status())) {
            throw badRequest("Only confirmed or checked-in bookings can be checked in");
        }
        if ("CONFIRMED".equals(booking.status()) && (today().isBefore(booking.checkIn()) || !today().isBefore(booking.checkOut()))) {
            throw badRequest("Check-in is allowed only within the booked stay window");
        }
        int guestCount = 1 + (request.companions() == null ? 0 : request.companions().size());
        if (guestCount > bookedCapacity(bookingId)) {
            throw badRequest("Check-in guests exceed booked capacity");
        }
        UUID checkInId = existingCheckInId(bookingId);
        if (checkInId == null) {
            checkInId = UUID.randomUUID();
            jdbcTemplate.update("""
                    insert into check_ins (id, booking_id, checked_in_by_account_id, checked_in_at, note)
                    values (:id, :bookingId, :accountId, now(), :note)
                    """, new MapSqlParameterSource()
                    .addValue("id", checkInId)
                    .addValue("bookingId", bookingId)
                    .addValue("accountId", user.accountId())
                    .addValue("note", trimToNull(request.note())));
        } else {
            jdbcTemplate.update("""
                    update check_ins
                    set note = :note
                    where id = :id
                    """, new MapSqlParameterSource("id", checkInId).addValue("note", trimToNull(request.note())));
            jdbcTemplate.update("delete from booking_guests where check_in_id = :checkInId",
                    new MapSqlParameterSource("checkInId", checkInId));
        }
        insertGuest(checkInId, request.primary(), true, 0);
        List<CheckInGuestRequest> companions = request.companions() == null ? List.of() : request.companions();
        for (int i = 0; i < companions.size(); i++) {
            insertGuest(checkInId, companions.get(i), false, i + 1);
        }
        jdbcTemplate.update("""
                update bookings
                set status = 'CHECKED_IN', updated_at = now()
                where id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId));
        return scopedHotelBookingDetail(hotelId, bookingId);
    }

    @Transactional
    public CheckInDetailResponse checkInDetail(UUID hotelId, UUID bookingId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        requireAction(user.accountId(), "bookings.check_in", hotelId);
        scopedHotelBookingDetail(hotelId, bookingId);
        CheckInSummary checkIn = loadCheckInSummary(bookingId);
        if (checkIn == null) {
            return new CheckInDetailResponse(null, List.of());
        }
        return new CheckInDetailResponse(checkIn, loadGuests(checkIn.id(), bookingId));
    }

    @Transactional
    public PaymentStartResponse startPayment(UUID bookingId, Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        expireBookingIfDue(bookingId);
        BookingForUpdate booking = lockBooking(bookingId);
        if (!booking.accountId().equals(user.accountId())) {
            throw notFound("Booking not found");
        }
        if (!"PENDING".equals(booking.status())) {
            throw badRequest("Only pending bookings can start payment");
        }
        List<PaymentStartResponse> active = jdbcTemplate.query("""
                select id, merchant_txn_ref, payment_url
                from payments
                where booking_id = :bookingId
                  and status in (:statuses)
                  and (expires_at is null or expires_at > now())
                order by created_at desc
                limit 1
                """, new MapSqlParameterSource("bookingId", bookingId).addValue("statuses", ACTIVE_PAYMENT_STATUSES),
                (rs, rowNum) -> new PaymentStartResponse(
                        (UUID) rs.getObject("id"),
                        rs.getString("merchant_txn_ref"),
                        rs.getString("payment_url")
                ));
        if (!active.isEmpty()) {
            return active.getFirst();
        }
        UUID paymentId = UUID.randomUUID();
        String merchantTxnRef = "BK_" + bookingId + "_" + now().toEpochMilli();
        String paymentUrl = "/payment-result?payment_status=pending&booking_id=" + bookingId;
        jdbcTemplate.update("""
                insert into payments (id, booking_id, status, amount, merchant_txn_ref, payment_url, expires_at)
                values (:id, :bookingId, 'INIT', :amount, :merchantTxnRef, :paymentUrl, :expiresAt)
                """, new MapSqlParameterSource()
                .addValue("id", paymentId)
                .addValue("bookingId", bookingId)
                .addValue("amount", booking.totalAmount())
                .addValue("merchantTxnRef", merchantTxnRef)
                .addValue("paymentUrl", paymentUrl)
                .addValue("expiresAt", timestamp(booking.pendingExpiresAt())));
        return new PaymentStartResponse(paymentId, merchantTxnRef, paymentUrl);
    }

    @Scheduled(fixedDelayString = "${app.bookings.expiry-fixed-delay-ms:60000}")
    @Transactional
    public void expireDuePendingBookings() {
        List<UUID> ids = jdbcTemplate.query("""
                select id
                from bookings
                where status = 'PENDING'
                  and pending_expires_at is not null
                  and pending_expires_at <= now()
                order by pending_expires_at
                limit 100
                for update skip locked
                """, new MapSqlParameterSource(), (rs, rowNum) -> (UUID) rs.getObject("id"));
        for (UUID id : ids) {
            transitionToCancelled(lockBooking(id), !hasSuccessfulPayment(id));
        }
    }

    @Transactional
    public void expireBookingIfDue(UUID bookingId) {
        List<BookingForUpdate> rows = jdbcTemplate.query("""
                select *
                from bookings
                where id = :bookingId
                  and status = 'PENDING'
                  and pending_expires_at is not null
                  and pending_expires_at <= now()
                for update
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> mapBookingForUpdate(rs));
        if (!rows.isEmpty()) {
            UUID id = rows.getFirst().id();
            transitionToCancelled(rows.getFirst(), !hasSuccessfulPayment(id));
        }
    }

    private void transitionToCancelled(BookingForUpdate booking, boolean restorePromotionUsage) {
        if ("CANCELLED".equals(booking.status())) {
            return;
        }
        if (!Set.of("PENDING", "CONFIRMED").contains(booking.status())) {
            throw badRequest("Booking cannot be cancelled from status " + booking.status());
        }
        int updated = jdbcTemplate.update("""
                update bookings
                set status = 'CANCELLED', cancelled_at = coalesce(cancelled_at, now()), updated_at = now()
                where id = :bookingId
                  and status in ('PENDING', 'CONFIRMED')
                """, new MapSqlParameterSource("bookingId", booking.id()));
        if (updated == 0) {
            return;
        }
        releaseInventory(booking.id(), booking.checkIn(), booking.checkOut());
        if (restorePromotionUsage && booking.promotionId() != null) {
            restorePromotionUsage(booking.promotionId());
        }
        cancelActivePayments(booking.id());
    }

    private void completeCheckout(BookingForUpdate booking) {
        if (!"CHECKED_IN".equals(booking.status())) {
            throw badRequest("Only checked-in bookings can be completed");
        }
        UUID checkInId = existingCheckInId(booking.id());
        if (checkInId == null) {
            throw badRequest("Checkout requires an existing check-in");
        }
        jdbcTemplate.update("""
                update check_ins
                set checked_out_at = coalesce(checked_out_at, now())
                where id = :checkInId
                """, new MapSqlParameterSource("checkInId", checkInId));
        jdbcTemplate.update("""
                update bookings
                set status = 'COMPLETED', completed_at = coalesce(completed_at, now()), updated_at = now()
                where id = :bookingId
                """, new MapSqlParameterSource("bookingId", booking.id()));
    }

    private void markNoShow(BookingForUpdate booking) {
        if (!"CONFIRMED".equals(booking.status())) {
            throw badRequest("Only confirmed bookings can be marked no-show");
        }
        if (today().isBefore(booking.checkIn())) {
            throw badRequest("No-show is allowed only on or after check-in date");
        }
        jdbcTemplate.update("""
                update bookings
                set status = 'NO_SHOW', no_show_at = coalesce(no_show_at, now()), updated_at = now()
                where id = :bookingId
                """, new MapSqlParameterSource("bookingId", booking.id()));
    }

    private void reserveInventory(UUID hotelId, LocalDate checkIn, LocalDate checkOut, List<BookingItemRequest> items) {
        for (BookingItemRequest item : items) {
            LocalDate date = checkIn;
            while (date.isBefore(checkOut)) {
                int updated = jdbcTemplate.update("""
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
                        .addValue("roomTypeId", item.roomTypeId())
                        .addValue("stayDate", date)
                        .addValue("quantity", item.quantity()));
                if (updated != 1) {
                    throw conflict("Not enough inventory for the selected stay dates");
                }
                date = date.plusDays(1);
            }
        }
    }

    private void releaseInventory(UUID bookingId, LocalDate checkIn, LocalDate checkOut) {
        List<BookingItemQuantity> items = jdbcTemplate.query("""
                select room_type_id, quantity
                from booking_items
                where booking_id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId),
                (rs, rowNum) -> new BookingItemQuantity((UUID) rs.getObject("room_type_id"), rs.getInt("quantity")));
        for (BookingItemQuantity item : items) {
            LocalDate date = checkIn;
            while (date.isBefore(checkOut)) {
                jdbcTemplate.update("""
                        update inventories
                        set available_rooms = least(total_rooms, available_rooms + :quantity),
                            updated_at = now()
                        where room_type_id = :roomTypeId
                          and stay_date = :stayDate
                        """, new MapSqlParameterSource()
                        .addValue("roomTypeId", item.roomTypeId())
                        .addValue("stayDate", date)
                        .addValue("quantity", item.quantity()));
                date = date.plusDays(1);
            }
        }
    }

    private BookingResponse scopedHotelBookingDetail(UUID hotelId, UUID bookingId) {
        return jdbcTemplate.query("""
                select *
                from bookings
                where id = :bookingId and hotel_id = :hotelId
                """, new MapSqlParameterSource("bookingId", bookingId).addValue("hotelId", hotelId),
                (rs, rowNum) -> mapBooking(rs, Audience.OPERATIONS)
        ).stream().findFirst().orElseThrow(() -> notFound("Booking not found"));
    }

    private BookingResponse bookingDetail(UUID bookingId, Audience audience, UUID accountId) {
        String accountPredicate = accountId == null ? "" : " and account_id = :accountId";
        return jdbcTemplate.query("""
                select *
                from bookings
                where id = :bookingId
                """ + accountPredicate,
                new MapSqlParameterSource("bookingId", bookingId).addValue("accountId", accountId),
                (rs, rowNum) -> mapBooking(rs, audience)
        ).stream().findFirst().orElseThrow(() -> notFound("Booking not found"));
    }

    private BookingResponse mapBooking(ResultSet rs, Audience audience) throws SQLException {
        UUID bookingId = (UUID) rs.getObject("id");
        UUID hotelId = (UUID) rs.getObject("hotel_id");
        UUID accountId = (UUID) rs.getObject("account_id");
        UUID promotionId = (UUID) rs.getObject("promotion_id");
        return new BookingResponse(
                bookingId,
                hotelId,
                accountId,
                rs.getString("booking_reference"),
                rs.getString("status"),
                rs.getObject("check_in", LocalDate.class),
                rs.getObject("check_out", LocalDate.class),
                rs.getString("guest_name"),
                rs.getString("guest_email"),
                rs.getString("guest_phone"),
                rs.getString("note"),
                rs.getBigDecimal("subtotal_amount"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("total_amount"),
                promotionId == null ? null : loadPromotionSummary(promotionId),
                loadHotelSummary(hotelId),
                audience == Audience.OPERATIONS ? loadUserSummary(accountId) : null,
                audience == Audience.OPERATIONS ? new CommissionSummary(
                        rs.getString("commission_package_code"),
                        rs.getBigDecimal("commission_rate"),
                        rs.getBigDecimal("commission_amount")
                ) : null,
                loadItems(bookingId),
                loadPayments(bookingId),
                loadCheckInSummary(bookingId),
                toInstant(rs, "pending_expires_at"),
                toInstant(rs, "cancelled_at"),
                toInstant(rs, "completed_at"),
                toInstant(rs, "no_show_at"),
                toInstant(rs, "created_at"),
                toInstant(rs, "updated_at")
        );
    }

    private List<BookingItemResponse> loadItems(UUID bookingId) {
        return jdbcTemplate.query("""
                select *
                from booking_items
                where booking_id = :bookingId
                order by created_at, room_type_name
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> new BookingItemResponse(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("booking_id"),
                (UUID) rs.getObject("room_type_id"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("line_total"),
                new RoomTypeSummary((UUID) rs.getObject("room_type_id"), rs.getString("room_type_name"), rs.getInt("max_guests"))
        ));
    }

    private List<PaymentSummary> loadPayments(UUID bookingId) {
        return jdbcTemplate.query("""
                select *
                from payments
                where booking_id = :bookingId
                order by created_at desc
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> new PaymentSummary(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("booking_id"),
                rs.getString("provider"),
                rs.getString("status"),
                rs.getBigDecimal("amount"),
                rs.getString("currency"),
                rs.getString("merchant_txn_ref"),
                toInstant(rs, "paid_at"),
                toInstant(rs, "expires_at"),
                toInstant(rs, "created_at"),
                toInstant(rs, "updated_at")
        ));
    }

    private CheckInSummary loadCheckInSummary(UUID bookingId) {
        return jdbcTemplate.query("""
                select ci.*,
                       (select count(*) from booking_guests bg where bg.check_in_id = ci.id) as guest_count
                from check_ins ci
                where ci.booking_id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> new CheckInSummary(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("booking_id"),
                (UUID) rs.getObject("checked_in_by_account_id"),
                toInstant(rs, "checked_in_at"),
                toInstant(rs, "checked_out_at"),
                rs.getString("note"),
                rs.getInt("guest_count")
        )).stream().findFirst().orElse(null);
    }

    private List<BookingGuestResponse> loadGuests(UUID checkInId, UUID bookingId) {
        return jdbcTemplate.query("""
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

    private HotelSummary loadHotelSummary(UUID hotelId) {
        return jdbcTemplate.query("""
                select id, name, address, city, country
                from hotels
                where id = :hotelId
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> new HotelSummary(
                (UUID) rs.getObject("id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("country")
        )).stream().findFirst().orElse(null);
    }

    private UserSummary loadUserSummary(UUID accountId) {
        return jdbcTemplate.query("""
                select id, email, first_name, last_name
                from accounts
                where id = :accountId
                """, new MapSqlParameterSource("accountId", accountId), (rs, rowNum) -> new UserSummary(
                (UUID) rs.getObject("id"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name")
        )).stream().findFirst().orElse(null);
    }

    private PromotionSummary loadPromotionSummary(UUID promotionId) {
        return jdbcTemplate.query("""
                select id, code
                from promotions
                where id = :promotionId
                """, new MapSqlParameterSource("promotionId", promotionId), (rs, rowNum) -> new PromotionSummary(
                (UUID) rs.getObject("id"),
                rs.getString("code")
        )).stream().findFirst().orElse(null);
    }

    private List<RoomTypeForBooking> loadRoomTypes(UUID hotelId, List<BookingItemRequest> items) {
        List<UUID> ids = items.stream().map(BookingItemRequest::roomTypeId).toList();
        List<RoomTypeForBooking> rows = jdbcTemplate.query("""
                select id, name, price_per_night, max_guests
                from room_types
                where hotel_id = :hotelId
                  and id in (:ids)
                  and active
                  and deleted_at is null
                """, new MapSqlParameterSource("hotelId", hotelId).addValue("ids", ids), (rs, rowNum) -> new RoomTypeForBooking(
                (UUID) rs.getObject("id"),
                rs.getString("name"),
                rs.getBigDecimal("price_per_night"),
                rs.getInt("max_guests")
        ));
        if (rows.size() != ids.size()) {
            throw badRequest("Room type IDs must belong to this hotel");
        }
        return rows;
    }

    private PromotionForBooking resolvePromotion(UUID hotelId, UUID accountId, String rawCode, BigDecimal subtotal) {
        String code = trimToNull(rawCode);
        if (code == null) {
            return null;
        }
        List<PromotionForBooking> promotions = jdbcTemplate.query("""
                select *
                from promotions
                where upper(code) = upper(:code)
                  and active
                  and (hotel_id is null or hotel_id = :hotelId)
                  and (starts_at is null or starts_at <= now())
                  and (ends_at is null or ends_at >= now())
                  and (min_booking_amount is null or min_booking_amount <= :subtotal)
                  and (total_usage_limit is null or used_count < total_usage_limit)
                for update
                """, new MapSqlParameterSource("code", code)
                .addValue("hotelId", hotelId)
                .addValue("subtotal", subtotal), (rs, rowNum) -> mapPromotion(rs, subtotal));
        if (promotions.isEmpty()) {
            throw badRequest("Promotion code is invalid");
        }
        PromotionForBooking promotion = promotions.getFirst();
        if (promotion.perUserUsageLimit() != null) {
            long usedByAccount = jdbcTemplate.queryForObject("""
                    select count(*)
                    from bookings
                    where account_id = :accountId
                      and promotion_id = :promotionId
                      and status <> 'CANCELLED'
                    """, new MapSqlParameterSource("accountId", accountId).addValue("promotionId", promotion.id()), Long.class);
            if (usedByAccount >= promotion.perUserUsageLimit()) {
                throw badRequest("Promotion usage limit has been reached");
            }
        }
        return promotion;
    }

    private PromotionForBooking mapPromotion(ResultSet rs, BigDecimal subtotal) throws SQLException {
        BigDecimal discount = switch (rs.getString("discount_type")) {
            case "PERCENT" -> subtotal.multiply(rs.getBigDecimal("discount_value")).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case "FIXED" -> rs.getBigDecimal("discount_value");
            default -> BigDecimal.ZERO;
        };
        BigDecimal maxDiscount = rs.getBigDecimal("max_discount");
        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            discount = maxDiscount;
        }
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }
        Integer perUserUsageLimit = (Integer) rs.getObject("per_user_usage_limit");
        return new PromotionForBooking((UUID) rs.getObject("id"), discount.setScale(2, RoundingMode.HALF_UP), perUserUsageLimit);
    }

    private CommissionSnapshot resolveCommission(UUID hotelId, BigDecimal total) {
        return jdbcTemplate.query("""
                select cp.code, cp.commission_rate, cp.active
                from hotel_commission_packages hcp
                join commission_packages cp on cp.id = hcp.commission_package_id
                where hcp.hotel_id = :hotelId
                union all
                select cp.code, cp.commission_rate, cp.active
                from commission_packages cp
                where cp.code = 'STANDARD'
                  and not exists (select 1 from hotel_commission_packages where hotel_id = :hotelId)
                limit 1
                """, new MapSqlParameterSource("hotelId", hotelId), (rs, rowNum) -> {
            String code = rs.getString("code");
            BigDecimal rate = rs.getBoolean("active") ? rs.getBigDecimal("commission_rate") : BigDecimal.ZERO;
            BigDecimal amount = total.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            return new CommissionSnapshot(code, rate, amount);
        }).stream().findFirst().orElse(new CommissionSnapshot(null, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    private void consumePromotion(UUID promotionId) {
        jdbcTemplate.update("update promotions set used_count = used_count + 1, updated_at = now() where id = :promotionId",
                new MapSqlParameterSource("promotionId", promotionId));
    }

    private void restorePromotionUsage(UUID promotionId) {
        jdbcTemplate.update("""
                update promotions
                set used_count = greatest(0, used_count - 1), updated_at = now()
                where id = :promotionId
                """, new MapSqlParameterSource("promotionId", promotionId));
    }

    private void cancelActivePayments(UUID bookingId) {
        jdbcTemplate.update("""
                update payments
                set status = 'CANCELED', updated_at = now()
                where booking_id = :bookingId
                  and status in (:statuses)
                """, new MapSqlParameterSource("bookingId", bookingId).addValue("statuses", ACTIVE_PAYMENT_STATUSES));
    }

    private boolean hasSuccessfulPayment(UUID bookingId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (
                    select 1 from payments where booking_id = :bookingId and status = 'SUCCEEDED'
                )
                """, new MapSqlParameterSource("bookingId", bookingId), Boolean.class);
        return Boolean.TRUE.equals(exists);
    }

    private void requireActiveHotel(UUID hotelId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from hotels
                    where id = :hotelId
                      and status = 'ACTIVE'
                      and deleted_at is null
                )
                """, new MapSqlParameterSource("hotelId", hotelId), Boolean.class);
        if (!Boolean.TRUE.equals(exists)) {
            throw notFound("Hotel not found");
        }
    }

    private BookingForUpdate lockBooking(UUID bookingId) {
        return jdbcTemplate.query("""
                select *
                from bookings
                where id = :bookingId
                for update
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> mapBookingForUpdate(rs))
                .stream().findFirst().orElseThrow(() -> notFound("Booking not found"));
    }

    private BookingForUpdate lockHotelBooking(UUID hotelId, UUID bookingId) {
        return jdbcTemplate.query("""
                select *
                from bookings
                where id = :bookingId and hotel_id = :hotelId
                for update
                """, new MapSqlParameterSource("bookingId", bookingId).addValue("hotelId", hotelId),
                (rs, rowNum) -> mapBookingForUpdate(rs)
        ).stream().findFirst().orElseThrow(() -> notFound("Booking not found"));
    }

    private BookingForUpdate mapBookingForUpdate(ResultSet rs) throws SQLException {
        return new BookingForUpdate(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("account_id"),
                (UUID) rs.getObject("hotel_id"),
                (UUID) rs.getObject("promotion_id"),
                rs.getString("status"),
                rs.getObject("check_in", LocalDate.class),
                rs.getObject("check_out", LocalDate.class),
                rs.getBigDecimal("total_amount"),
                toInstant(rs, "pending_expires_at")
        );
    }

    private UUID existingCheckInId(UUID bookingId) {
        return jdbcTemplate.query("""
                select id
                from check_ins
                where booking_id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId), (rs, rowNum) -> (UUID) rs.getObject("id"))
                .stream().findFirst().orElse(null);
    }

    private int bookedCapacity(UUID bookingId) {
        Integer capacity = jdbcTemplate.queryForObject("""
                select coalesce(sum(quantity * max_guests), 0)
                from booking_items
                where booking_id = :bookingId
                """, new MapSqlParameterSource("bookingId", bookingId), Integer.class);
        return capacity == null ? 0 : capacity;
    }

    private void insertGuest(UUID checkInId, CheckInGuestRequest guest, boolean primary, int order) {
        jdbcTemplate.update("""
                insert into booking_guests (id, check_in_id, full_name, identity_number, phone, is_primary, guest_order)
                values (:id, :checkInId, :fullName, :identityNumber, :phone, :primary, :guestOrder)
                """, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("checkInId", checkInId)
                .addValue("fullName", trimRequired(guest.fullName(), "Guest fullName is required"))
                .addValue("identityNumber", trimToNull(guest.identityNumber()))
                .addValue("phone", trimToNull(guest.phone()))
                .addValue("primary", primary)
                .addValue("guestOrder", order));
    }

    private void validateCreateRequest(BookingCreateRequest request) {
        if (request.checkIn() == null || request.checkOut() == null || !request.checkOut().isAfter(request.checkIn())) {
            throw badRequest("checkOut must be after checkIn");
        }
        if (request.checkIn().isBefore(today())) {
            throw badRequest("checkIn cannot be in the past");
        }
        trimRequired(request.guestName(), "guestName is required");
        trimRequired(request.guestEmail(), "guestEmail is required");
        trimRequired(request.guestPhone(), "guestPhone is required");
        if (request.note() != null && request.note().length() > 1000) {
            throw badRequest("note must be at most 1000 characters");
        }
    }

    private List<BookingItemRequest> distinctItems(List<BookingItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw badRequest("Booking items are required");
        }
        Set<UUID> seen = new HashSet<>();
        List<BookingItemRequest> distinct = new ArrayList<>();
        for (BookingItemRequest item : items) {
            if (item == null || item.roomTypeId() == null || item.quantity() == null || item.quantity() <= 0) {
                throw badRequest("Booking items require roomTypeId and positive quantity");
            }
            if (!seen.add(item.roomTypeId())) {
                throw badRequest("Duplicate roomTypeId is not allowed");
            }
            distinct.add(item);
        }
        return distinct;
    }

    private CurrentUser requireVerifiedUser(Authentication authentication) {
        CurrentUser user = requireUser(authentication);
        if (!user.emailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email verification is required");
        }
        return user;
    }

    private CurrentUser requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountAuthUser account)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return new CurrentUser(account.getAccountId(), account.isEmailVerified());
    }

    private void requireAction(UUID accountId, String actionKey, UUID hotelId) {
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
                          or ap.scope = 'SELF'
                          or (
                              cast(:hotelId as uuid) is not null
                              and ap.scope = 'HOTEL_MEMBER'
                              and exists (
                                  select 1 from hotel_members hm
                                  where hm.hotel_id = :hotelId and hm.account_id = :accountId
                              )
                          )
                          or (
                              cast(:hotelId as uuid) is not null
                              and ap.scope = 'HOTEL_OWNER'
                              and exists (
                                  select 1 from hotels h
                                  where h.id = :hotelId and h.owner_id = :accountId
                              )
                          )
                      )
                )
                """, new MapSqlParameterSource("actionKey", actionKey)
                .addValue("accountId", accountId)
                .addValue("hotelId", hotelId, Types.OTHER), Boolean.class);
        if (!Boolean.TRUE.equals(allowed)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action not allowed: " + actionKey);
        }
    }

    private void requirePermission(UUID accountId, String permissionKey) {
        Boolean allowed = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from account_roles ar
                    join role_permissions rp on rp.role_id = ar.role_id
                    join permissions p on p.id = rp.permission_id
                    where ar.account_id = :accountId
                      and p.key = :permissionKey
                )
                """, new MapSqlParameterSource("accountId", accountId).addValue("permissionKey", permissionKey), Boolean.class);
        if (!Boolean.TRUE.equals(allowed)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission not allowed: " + permissionKey);
        }
    }

    private String normalizeOptionalStatus(String status) {
        if (trimToNull(status) == null) {
            return null;
        }
        return normalizeStatus(status);
    }

    private String normalizeStatus(String status) {
        String normalized = trimRequired(status, "Booking status is required").toUpperCase(Locale.ROOT);
        if (!BOOKING_STATUSES.contains(normalized)) {
            throw badRequest("Invalid booking status");
        }
        return normalized;
    }

    private String likeQuery(String q) {
        String trimmed = trimToNull(q);
        return trimmed == null ? null : "%" + trimmed.toLowerCase(Locale.ROOT) + "%";
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Instant now() {
        return clock.instant();
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(BUSINESS_ZONE));
    }

    private Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private Instant toInstant(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column) == null ? null : rs.getTimestamp(column).toInstant();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private enum Audience {
        CUSTOMER,
        OPERATIONS
    }

    private record CurrentUser(UUID accountId, boolean emailVerified) {
    }

    private record RoomTypeForBooking(UUID id, String name, BigDecimal pricePerNight, int maxGuests) {
    }

    private record BookingLine(UUID id, RoomTypeForBooking roomType, int quantity, BigDecimal lineTotal) {
    }

    private record BookingItemQuantity(UUID roomTypeId, int quantity) {
    }

    private record PromotionForBooking(UUID id, BigDecimal discountAmount, Integer perUserUsageLimit) {
    }

    private record CommissionSnapshot(String packageCode, BigDecimal rate, BigDecimal amount) {
    }

    private record BookingForUpdate(
            UUID id,
            UUID accountId,
            UUID hotelId,
            UUID promotionId,
            String status,
            LocalDate checkIn,
            LocalDate checkOut,
            BigDecimal totalAmount,
            Instant pendingExpiresAt
    ) {
    }
}
