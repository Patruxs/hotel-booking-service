package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.FrontendProperties;
import org.example.hotelbookingservice.config.VnpayProperties;
import org.example.hotelbookingservice.security.vnpay.VnpaySigner;

import org.example.hotelbookingservice.dto.request.booking.operations.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.operations.BookingItemRequest;
import org.example.hotelbookingservice.dto.response.booking.operations.BookingResponse;
import org.example.hotelbookingservice.dto.request.booking.operations.CheckInGuestRequest;
import org.example.hotelbookingservice.dto.request.booking.operations.CheckInRequest;
import org.example.hotelbookingservice.dto.request.booking.operations.PaymentStartRequest;
import org.example.hotelbookingservice.dto.response.booking.operations.PaymentStartResponse;
import org.example.hotelbookingservice.repository.operations.BookingOperationsRepository;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class BookingOperationsServiceImplIntegrationTest {
    private static final UUID CUSTOMER_ID = UUID.fromString("30000000-0000-4000-8000-000000000001");
    private static final UUID OTHER_CUSTOMER_ID = UUID.fromString("30000000-0000-4000-8000-000000000002");
    private static final UUID OWNER_ID = UUID.fromString("30000000-0000-4000-8000-000000000003");
    private static final UUID HOTEL_ID = UUID.fromString("30000000-0000-4000-8000-000000000004");
    private static final UUID OTHER_HOTEL_ID = UUID.fromString("30000000-0000-4000-8000-000000000005");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("30000000-0000-4000-8000-000000000006");
    private static final UUID PROMOTION_ID = UUID.fromString("30000000-0000-4000-8000-000000000007");
    private static final UUID ADMIN_ID = UUID.fromString("30000000-0000-4000-8000-000000000010");
    private static final UUID RECEPTIONIST_ID = UUID.fromString("30000000-0000-4000-8000-000000000011");
    private static final LocalDate CHECK_IN = LocalDate.of(2027, 7, 1);
    private static final LocalDate CHECK_OUT = LocalDate.of(2027, 7, 3);

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hotel_booking_service_booking_test")
            .withUsername("test")
            .withPassword("test");

    static AnnotationConfigApplicationContext context;
    static JdbcTemplate jdbc;
    static BookingOperationsServiceImpl service;

    @BeforeAll
    static void migrate() {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        jdbc = new JdbcTemplate(dataSource);

        context = new AnnotationConfigApplicationContext();
        context.registerBean(DataSource.class, () -> dataSource);
        context.registerBean(NamedParameterJdbcTemplate.class, () -> new NamedParameterJdbcTemplate(dataSource));
        context.registerBean(BookingOperationsRepository.class, () -> new BookingOperationsRepository(
                context.getBean(NamedParameterJdbcTemplate.class)
        ));
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        context.registerBean(DataSourceTransactionManager.class, () -> transactionManager);
        context.registerBean(Clock.class, () -> Clock.fixed(Instant.parse("2027-07-01T00:00:00Z"), ZoneOffset.UTC));
        context.register(TransactionalTestConfig.class);
        context.registerBean(VnpayProperties.class, () -> new VnpayProperties(null, null, null, null, true));
        context.registerBean(FrontendProperties.class, () -> new FrontendProperties(null));
        context.refresh();
        service = new BookingOperationsServiceImpl(
                context.getBean(BookingOperationsRepository.class),
                context.getBean(Clock.class),
                transactionManager,
                context.getBean(VnpayProperties.class),
                context.getBean(FrontendProperties.class)
        );
    }

    @AfterAll
    static void closeContext() {
        if (context != null) {
            context.close();
        }
    }

    @BeforeEach
    void resetData() {
        jdbc.update("delete from payment_events");
        jdbc.update("delete from payments");
        jdbc.update("delete from booking_guests");
        jdbc.update("delete from check_ins");
        jdbc.update("delete from reviews");
        jdbc.update("delete from booking_items");
        jdbc.update("delete from bookings");
        jdbc.update("delete from inventories");
        jdbc.update("delete from rooms");
        jdbc.update("delete from room_type_amenities");
        jdbc.update("delete from room_types");
        jdbc.update("delete from hotel_commission_packages");
        jdbc.update("delete from promotions");
        jdbc.update("delete from hotel_members");
        jdbc.update("delete from hotels");
        jdbc.update("delete from account_roles");
        jdbc.update("delete from accounts");

        insertAccount(CUSTOMER_ID, "customer@example.com", true);
        insertAccount(OTHER_CUSTOMER_ID, "other@example.com", true);
        insertAccount(OWNER_ID, "owner@example.com", true);
        insertAccount(RECEPTIONIST_ID, "receptionist@example.com", true);
        assignRole(CUSTOMER_ID, "CUSTOMER");
        assignRole(OTHER_CUSTOMER_ID, "CUSTOMER");
        assignRole(OWNER_ID, "OWNER");
        assignRole(RECEPTIONIST_ID, "RECEPTIONIST");
        insertHotel(HOTEL_ID, OWNER_ID, "booking-hotel", "ACTIVE");
        insertHotel(OTHER_HOTEL_ID, OWNER_ID, "other-booking-hotel", "ACTIVE");
        jdbc.update("insert into hotel_members (hotel_id, account_id) values (?, ?)", HOTEL_ID, OWNER_ID);
        jdbc.update("insert into hotel_members (hotel_id, account_id) values (?, ?)", OTHER_HOTEL_ID, OWNER_ID);
        jdbc.update("insert into hotel_members (hotel_id, account_id) values (?, ?)", HOTEL_ID, RECEPTIONIST_ID);
        jdbc.update("""
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Deluxe', 100.00, 2)
                """, ROOM_TYPE_ID, HOTEL_ID);
        insertInventory(ROOM_TYPE_ID, CHECK_IN, 2, 2);
        insertInventory(ROOM_TYPE_ID, CHECK_IN.plusDays(1), 2, 2);
        jdbc.update("""
                insert into promotions (
                    id, hotel_id, code, name, discount_type, discount_value, max_discount,
                    min_booking_amount, total_usage_limit, per_user_usage_limit, active
                ) values (?, ?, 'SAVE10', 'Save 10 percent', 'PERCENT', 10.00, 50.00, 100.00, 10, 1, true)
                """, PROMOTION_ID, HOTEL_ID);
    }

    @Test
    void createBookingCalculatesTotalsSnapshotsAndReservesInventory() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest("SAVE10", 1), customerAuth());

        assertThat(booking.status()).isEqualTo("PENDING");
        assertThat(booking.subtotalAmount()).isEqualByComparingTo("200.00");
        assertThat(booking.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(booking.totalAmount()).isEqualByComparingTo("180.00");
        assertThat(booking.items()).hasSize(1);
        assertThat(booking.items().getFirst().unitPrice()).isEqualByComparingTo("100.00");
        assertThat(booking.commission()).isNull();
        assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(1);
        assertThat(inventoryAvailable(CHECK_IN.plusDays(1))).isEqualTo(1);
        assertThat(promotionUsedCount()).isEqualTo(1);

        BookingResponse operationsDetail = service.hotelBookingDetail(HOTEL_ID, booking.id(), ownerAuth());
        assertThat(operationsDetail.user()).isNotNull();
        assertThat(operationsDetail.commission().packageCode()).isEqualTo("STANDARD");
        assertThat(operationsDetail.commission().amount()).isEqualByComparingTo("18.00");
    }

    @Test
    void duplicateItemsAndUnverifiedAccountsAreRejectedBeforeInventoryChanges() {
        BookingCreateRequest duplicateItems = new BookingCreateRequest(
                CHECK_IN,
                CHECK_OUT,
                "Guest",
                "guest@example.com",
                "0900000000",
                null,
                null,
                BigDecimal.ZERO,
                List.of(new BookingItemRequest(ROOM_TYPE_ID, 1), new BookingItemRequest(ROOM_TYPE_ID, 1))
        );

        assertThatThrownBy(() -> service.createBooking(HOTEL_ID, duplicateItems, customerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Duplicate roomTypeId");

        insertAccount(UUID.fromString("30000000-0000-4000-8000-000000000008"), "unverified@example.com", false);
        assignRole(UUID.fromString("30000000-0000-4000-8000-000000000008"), "CUSTOMER");
        assertThatThrownBy(() -> service.createBooking(HOTEL_ID, createRequest(null, 1), auth(
                UUID.fromString("30000000-0000-4000-8000-000000000008"),
                "CUSTOMER",
                false
        ))).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email verification is required");

        assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(2);
        assertThat(promotionUsedCount()).isZero();
    }

    @Test
    void inventoryReservationDoesNotOversellUnderConcurrentAttempts() throws Exception {
        jdbc.update("update inventories set available_rooms = 1 where room_type_id = ?", ROOM_TYPE_ID);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            executor.submit(() -> {
                await(start);
                try {
                    service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
                    successes.incrementAndGet();
                } catch (ResponseStatusException exception) {
                    conflicts.incrementAndGet();
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(1);
        assertThat(inventoryAvailable(CHECK_IN)).isZero();
        assertThat(inventoryAvailable(CHECK_IN.plusDays(1))).isZero();
    }

    @Test
    void customerScopingCancellationAndExpiryReleaseInventoryAndPromotionUsage() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest("SAVE10", 1), customerAuth());

        assertThatThrownBy(() -> service.mineDetail(booking.id(), otherCustomerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
        assertThatThrownBy(() -> service.hotelBookingDetail(OTHER_HOTEL_ID, booking.id(), ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");

        BookingResponse cancelled = service.cancelMine(booking.id(), customerAuth());
        assertThat(cancelled.status()).isEqualTo("CANCELLED");
        assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(2);
        assertThat(promotionUsedCount()).isZero();

        BookingResponse expiring = service.createBooking(HOTEL_ID, createRequest("SAVE10", 1), customerAuth());
        UUID paymentId = UUID.fromString("30000000-0000-4000-8000-000000000009");
        jdbc.update("""
                insert into payments (id, booking_id, status, amount, merchant_txn_ref, payment_url)
                values (?, ?, 'INIT', 180.00, 'BK_EXPIRING_PAYMENT', '/pay')
                """, paymentId, expiring.id());
        jdbc.update("update bookings set pending_expires_at = now() - interval '1 minute' where id = ?", expiring.id());
        service.expireDuePendingBookings();

        assertThat(service.mineDetail(expiring.id(), customerAuth()).status()).isEqualTo("CANCELLED");
        assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(2);
        assertThat(promotionUsedCount()).isZero();
        assertThat(paymentStatus(paymentId)).isEqualTo("CANCELED");
    }

    @Test
      void ownerCanCancelHotelBookingAndMismatchedHotelMutationsLeaveStateUnchanged() {
          BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
          assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(1);

        assertThatThrownBy(() -> service.updateStatus(OTHER_HOTEL_ID, booking.id(), "CANCELLED", ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
        assertThatThrownBy(() -> service.cancelHotelBooking(OTHER_HOTEL_ID, booking.id(), ownerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
        assertThat(service.hotelBookingDetail(HOTEL_ID, booking.id(), ownerAuth()).status()).isEqualTo("PENDING");
        assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(1);

          assertThat(service.cancelHotelBooking(HOTEL_ID, booking.id(), ownerAuth()).status()).isEqualTo("CANCELLED");
          assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(2);
      }

      @Test
      void adminCanConfirmPendingBookingWithoutCreatingSuccessfulPayment() {
          insertAccount(ADMIN_ID, "admin-confirmation@example.com", true);
          assignRole(ADMIN_ID, "ADMIN");
          BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());

          BookingResponse confirmed = service.updateStatus(
                  HOTEL_ID,
                  booking.id(),
                  "CONFIRMED",
                  auth(ADMIN_ID, "ADMIN", true)
          );

          assertThat(confirmed.status()).isEqualTo("CONFIRMED");
          assertThat(bookingStatus(booking.id())).isEqualTo("CONFIRMED");
          assertThat(jdbc.queryForObject(
                  "select count(*) from payments where booking_id = ? and status = 'SUCCEEDED'",
                  Integer.class,
                  booking.id()
          )).isZero();
      }

      @Test
      void unfilteredCustomerAndHotelBookingListsDoNotBindNullStatusPredicates() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());

        assertThat(service.listMine(10, 0, null, customerAuth()).data())
                .extracting(BookingResponse::id)
                .contains(booking.id());
        assertThat(service.listHotelBookings(HOTEL_ID, 10, 0, null, null, ownerAuth()).data())
                .extracting(BookingResponse::id)
                .contains(booking.id());
    }

    @Test
    void checkInEditsGuestsAndCheckoutRequiresCheckIn() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        jdbc.update("update bookings set status = 'CONFIRMED' where id = ?", booking.id());

        service.checkIn(HOTEL_ID, booking.id(), new CheckInRequest(
                "Initial",
                new CheckInGuestRequest(null, "Primary Guest", "ID-1", "0900000001", null, null, null, null),
                List.of(new CheckInGuestRequest(null, "Companion", "ID-2", "0900000002", null, null, null, null))
        ), ownerAuth());

        assertThat(service.checkInDetail(HOTEL_ID, booking.id(), ownerAuth()).guests())
                .extracting(BookingOperationsServiceImplIntegrationTest::guestName)
                .containsExactly("Primary Guest", "Companion");

        BookingResponse checkedIn = service.checkIn(HOTEL_ID, booking.id(), new CheckInRequest(
                "Edited",
                new CheckInGuestRequest(null, "Edited Primary", "ID-3", "0900000003", null, null, null, null),
                List.of()
        ), ownerAuth());
        assertThat(checkedIn.status()).isEqualTo("CHECKED_IN");
        assertThat(service.checkInDetail(HOTEL_ID, booking.id(), ownerAuth()).guests())
                .extracting(BookingOperationsServiceImplIntegrationTest::guestName)
                .containsExactly("Edited Primary");

        BookingResponse completed = service.updateStatus(HOTEL_ID, booking.id(), "COMPLETED", ownerAuth());
        assertThat(completed.status()).isEqualTo("COMPLETED");
    }

    @Test
    void noShowKeepsInventoryAndPromotionUsageConsumed() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest("SAVE10", 1), customerAuth());
        jdbc.update("update bookings set status = 'CONFIRMED' where id = ?", booking.id());

        BookingResponse noShow = service.updateStatus(HOTEL_ID, booking.id(), "NO_SHOW", ownerAuth());

        assertThat(noShow.status()).isEqualTo("NO_SHOW");
        assertThat(inventoryAvailable(CHECK_IN)).isEqualTo(1);
        assertThat(inventoryAvailable(CHECK_IN.plusDays(1))).isEqualTo(1);
        assertThat(promotionUsedCount()).isEqualTo(1);
    }

    @Test
    void vnpayPaymentCreationSignsUrlDefaultsLocaleAndReusesActiveAttempt() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());

        PaymentStartResponse payment = service.startPayment(booking.id(), null, customerAuth());
        PaymentStartResponse reused = service.startPayment(booking.id(), new PaymentStartRequest(null, null), customerAuth());
        Map<String, String> query = parseQuery(payment.paymentUrl());

        assertThat(payment.paymentId()).isEqualTo(reused.paymentId());
        assertThat(payment.paymentUrl()).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        assertThat(payment.merchantTxnRef()).startsWith("BK_" + booking.id() + "_");
        assertThat(query).containsEntry("vnp_TxnRef", payment.merchantTxnRef());
        assertThat(query).containsEntry("vnp_Locale", "vn");
        assertThat(query).doesNotContainKey("vnp_BankCode");
        assertThat(VnpaySigner.verify(query, "DEMO_SECRET")).isTrue();
        assertThat(paymentStatus(payment.paymentId())).isEqualTo("PENDING");
        assertThat(paymentEventCount(payment.paymentId(), "VNPAY_PAYMENT_URL_CREATED")).isEqualTo(1);
    }

    @Test
    void disabledVnpayPaymentStartReturns503WithoutPersistenceSideEffects() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        BookingOperationsServiceImpl disabledService = new BookingOperationsServiceImpl(
                context.getBean(BookingOperationsRepository.class),
                context.getBean(Clock.class),
                context.getBean(DataSourceTransactionManager.class),
                new VnpayProperties(null, null, null, null, false),
                context.getBean(FrontendProperties.class)
        );

        assertThatThrownBy(() -> disabledService.startPayment(booking.id(), null, customerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThat(jdbc.queryForObject("select count(*) from payments where booking_id = ?", Integer.class, booking.id()))
                .isZero();
        assertThat(jdbc.queryForObject("select count(*) from payment_events", Integer.class)).isZero();
    }

    @Test
    void expiredPaymentAttemptIsCanceledBeforeReplacement() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        PaymentStartResponse first = service.startPayment(booking.id(), null, customerAuth());
        jdbc.update("update payments set expires_at = now() - interval '1 minute' where id = ?", first.paymentId());

        PaymentStartResponse replacement = service.startPayment(booking.id(), new PaymentStartRequest("en", "NCB"), customerAuth());
        Map<String, String> query = parseQuery(replacement.paymentUrl());

        assertThat(replacement.paymentId()).isNotEqualTo(first.paymentId());
        assertThat(replacement.merchantTxnRef()).isNotEqualTo(first.merchantTxnRef());
        assertThat(paymentStatus(first.paymentId())).isEqualTo("CANCELED");
        assertThat(paymentEventCount(first.paymentId(), "PAYMENT_ATTEMPT_CANCELED_BEFORE_RETRY")).isEqualTo(1);
        assertThat(query).containsEntry("vnp_Locale", "en");
        assertThat(query).containsEntry("vnp_BankCode", "NCB");
    }

    @Test
    void normalVnpaySuccessConfirmsBookingOnceAndRecordsMailEventAfterCommit() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        PaymentStartResponse payment = service.startPayment(booking.id(), null, customerAuth());

        Map<String, String> ipn = service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "20000", "00"));
        Map<String, String> duplicate = service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "20000", "00"));

        assertThat(ipn).containsEntry("RspCode", "00").containsEntry("Message", "Confirm Success");
        assertThat(duplicate).containsEntry("RspCode", "02");
        assertThat(paymentStatus(payment.paymentId())).isEqualTo("SUCCEEDED");
        assertThat(bookingStatus(booking.id())).isEqualTo("CONFIRMED");
        assertThat(paymentEventCount(payment.paymentId(), "VNPAY_PAYMENT_SUCCEEDED")).isEqualTo(1);
        assertThat(paymentEventCount(payment.paymentId(), "MAIL_PAYMENT_SUCCEEDED")).isEqualTo(1);
    }

    @Test
    void providerFailureDoesNotConfirmBooking() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        PaymentStartResponse payment = service.startPayment(booking.id(), null, customerAuth());

        Map<String, String> ipn = service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "20000", "24"));

        assertThat(ipn).containsEntry("RspCode", "00");
        assertThat(paymentStatus(payment.paymentId())).isEqualTo("FAILED");
        assertThat(bookingStatus(booking.id())).isEqualTo("PENDING");
        assertThat(paymentEventCount(payment.paymentId(), "VNPAY_PAYMENT_FAILED")).isEqualTo(1);
    }

    @Test
    void lateSuccessRequiresReviewAndCreatesAdminReconciliationNotification() {
        insertAccount(ADMIN_ID, "admin-payments@example.com", true);
        assignRole(ADMIN_ID, "ADMIN");
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        PaymentStartResponse payment = service.startPayment(booking.id(), null, customerAuth());
        service.cancelMine(booking.id(), customerAuth());

        String redirect = service.handleVnpayReturn(signedCallback(payment.merchantTxnRef(), "20000", "00"));
        Map<String, String> duplicate = service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "20000", "00"));

        assertThat(redirect).contains("payment_status=requires_review").contains("booking_id=" + booking.id());
        assertThat(duplicate).containsEntry("RspCode", "02");
        assertThat(paymentStatus(payment.paymentId())).isEqualTo("LATE_SUCCEEDED");
        assertThat(bookingStatus(booking.id())).isEqualTo("CANCELLED");
        assertThat(paymentEventCount(payment.paymentId(), "LATE_SUCCEEDED_RECONCILIATION_CREATED")).isEqualTo(1);
        assertThat(paymentEventCount(payment.paymentId(), "VNPAY_DUPLICATE_CALLBACK")).isEqualTo(1);
        assertThat(notificationCount(ADMIN_ID)).isEqualTo(1);
    }

    @Test
    void concurrentSuccessfulCallbacksOnlyConfirmAndSendMailOnce() throws Exception {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        PaymentStartResponse payment = service.startPayment(booking.id(), null, customerAuth());
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        var first = executor.submit(() -> {
            await(start);
            service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "20000", "00"));
        });
        var second = executor.submit(() -> {
            await(start);
            service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "20000", "00"));
        });

        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        first.get();
        second.get();

        assertThat(paymentStatus(payment.paymentId())).isEqualTo("SUCCEEDED");
        assertThat(bookingStatus(booking.id())).isEqualTo("CONFIRMED");
        assertThat(paymentEventCount(payment.paymentId(), "VNPAY_PAYMENT_SUCCEEDED")).isEqualTo(1);
        assertThat(paymentEventCount(payment.paymentId(), "MAIL_PAYMENT_SUCCEEDED")).isEqualTo(1);
        assertThat(paymentEventCount(payment.paymentId(), "VNPAY_DUPLICATE_CALLBACK")).isEqualTo(1);
    }

    @Test
    void invalidVnpayChecksumOrderAndAmountUseProviderResponseCodes() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        PaymentStartResponse payment = service.startPayment(booking.id(), null, customerAuth());

        Map<String, String> badChecksum = signedCallback(payment.merchantTxnRef(), "20000", "00");
        badChecksum.put("vnp_Amount", "99999");

        assertThat(service.handleVnpayIpn(badChecksum)).containsEntry("RspCode", "97");
        assertThat(service.handleVnpayIpn(signedCallback("BK_missing_1", "20000", "00"))).containsEntry("RspCode", "01");
        assertThat(service.handleVnpayIpn(signedCallback(payment.merchantTxnRef(), "19999", "00"))).containsEntry("RspCode", "04");
        assertThat(paymentStatus(payment.paymentId())).isEqualTo("PENDING");
    }

    @Test
    void adminWithoutHotelMembershipCanAccessHotelScopedBookingActions() {
        // Admin holds the ADMIN role but is NOT a member of HOTEL_ID (real, non-seeded data).
        insertAccount(ADMIN_ID, "admin-access@example.com", true);
        assignRole(ADMIN_ID, "ADMIN");
        Authentication adminAuth = auth(ADMIN_ID, "ADMIN", true);
        assertThat(hotelMemberExists(HOTEL_ID, ADMIN_ID)).isFalse();

        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());
        jdbc.update("update bookings set status = 'CONFIRMED' where id = ?", booking.id());

        // bookings.list.hotel (HOTEL_MEMBER scope) — previously 403 for a non-member admin.
        assertThat(service.listHotelBookings(HOTEL_ID, 10, 0, null, null, adminAuth).data())
                .extracting(BookingResponse::id)
                .contains(booking.id());

        // bookings.check_in (HOTEL_MEMBER scope)
        BookingResponse checkedIn = service.checkIn(HOTEL_ID, booking.id(), new CheckInRequest(
                "Admin check-in",
                new CheckInGuestRequest(null, "Primary Guest", "ID-1", "0900000001", null, null, null, null),
                List.of()
        ), adminAuth);
        assertThat(checkedIn.status()).isEqualTo("CHECKED_IN");

        // bookings.status.update (HOTEL_MEMBER scope)
        BookingResponse completed = service.updateStatus(HOTEL_ID, booking.id(), "COMPLETED", adminAuth);
        assertThat(completed.status()).isEqualTo("COMPLETED");
    }

    @Test
    void adminPassesPermissionCheckEvenWhenPermissionNotGrantedToAdminRole() {
        // Prove the override is role-based, independent of granted permissions: strip
        // bookings.create from the ADMIN role, then confirm admin still passes requirePermission.
        insertAccount(ADMIN_ID, "admin-access@example.com", true);
        assignRole(ADMIN_ID, "ADMIN");
        UUID adminRoleId = jdbc.queryForObject("select id from roles where name = 'ADMIN'", UUID.class);
        UUID bookingsCreateId = jdbc.queryForObject("select id from permissions where key = 'bookings.create'", UUID.class);
        jdbc.update("delete from role_permissions where role_id = ? and permission_id = ?", adminRoleId, bookingsCreateId);
        try {
            BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), auth(ADMIN_ID, "ADMIN", true));
            assertThat(booking.status()).isEqualTo("PENDING");
        } finally {
            jdbc.update("insert into role_permissions (role_id, permission_id) values (?, ?) on conflict do nothing",
                    adminRoleId, bookingsCreateId);
        }
    }

    @Test
    void nonAdminNonMemberIsDeniedHotelScopedBookingActions() {
        // OTHER_CUSTOMER_ID has the CUSTOMER role and is not a member of HOTEL_ID.
        service.createBooking(HOTEL_ID, createRequest(null, 1), customerAuth());

        assertThatThrownBy(() -> service.listHotelBookings(HOTEL_ID, 10, 0, null, null, otherCustomerAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Action not allowed");
    }

    @Test
    void receptionistCreatesGuestBookingOnlyForAssignedHotel() {
        BookingResponse booking = service.createBooking(HOTEL_ID, createRequest(null, 1), receptionistAuth());

        assertThat(booking.status()).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("select account_id from bookings where id = ?", UUID.class, booking.id()))
                .isEqualTo(RECEPTIONIST_ID);
        assertThatThrownBy(() -> service.createBooking(OTHER_HOTEL_ID, createRequest(null, 1), receptionistAuth()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Action not allowed: bookings.create");
    }

    private boolean hotelMemberExists(UUID hotelId, UUID accountId) {
        Boolean exists = jdbc.queryForObject("""
                select exists (
                    select 1 from hotel_members where hotel_id = ? and account_id = ?
                )
                """, Boolean.class, hotelId, accountId);
        return Boolean.TRUE.equals(exists);
    }

    private static String guestName(org.example.hotelbookingservice.dto.response.booking.operations.BookingGuestResponse guest) {
        return guest.fullName();
    }

    private BookingCreateRequest createRequest(String promotionCode, int quantity) {
        return new BookingCreateRequest(
                CHECK_IN,
                CHECK_OUT,
                " Guest Name ",
                "guest@example.com",
                "0900000000",
                "Late arrival",
                promotionCode,
                BigDecimal.ZERO,
                List.of(new BookingItemRequest(ROOM_TYPE_ID, quantity))
        );
    }

    private void insertAccount(UUID accountId, String email, boolean verified) {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, ?, 'hash', 'Test', 'Account', ?)
                """, accountId, email, verified);
    }

    private void assignRole(UUID accountId, String roleName) {
        jdbc.update("""
                insert into account_roles (account_id, role_id)
                select ?, id
                from roles
                where name = ?
                """, accountId, roleName);
    }

    private void insertHotel(UUID hotelId, UUID ownerId, String slug, String status) {
        jdbc.update("""
                insert into hotels (id, owner_id, name, slug, address, city, country, status)
                values (?, ?, 'Booking Hotel', ?, '123 Booking Street', 'Da Nang', 'Vietnam', ?)
                """, hotelId, ownerId, slug, status);
    }

    private void insertInventory(UUID roomTypeId, LocalDate date, int totalRooms, int availableRooms) {
        jdbc.update("""
                insert into inventories (id, hotel_id, room_type_id, stay_date, total_rooms, available_rooms)
                values (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID(), HOTEL_ID, roomTypeId, date, totalRooms, availableRooms);
    }

    private int inventoryAvailable(LocalDate date) {
        return jdbc.queryForObject("""
                select available_rooms
                from inventories
                where room_type_id = ? and stay_date = ?
                """, Integer.class, ROOM_TYPE_ID, date);
    }

    private int promotionUsedCount() {
        return jdbc.queryForObject("select used_count from promotions where id = ?", Integer.class, PROMOTION_ID);
    }

    private String paymentStatus(UUID paymentId) {
        return jdbc.queryForObject("select status from payments where id = ?", String.class, paymentId);
    }

    private String bookingStatus(UUID bookingId) {
        return jdbc.queryForObject("select status from bookings where id = ?", String.class, bookingId);
    }

    private int paymentEventCount(UUID paymentId, String eventType) {
        return jdbc.queryForObject("""
                select count(*)
                from payment_events
                where payment_id = ? and event_type = ?
                """, Integer.class, paymentId, eventType);
    }

    private int notificationCount(UUID recipientAccountId) {
        return jdbc.queryForObject("select count(*) from notifications where recipient_account_id = ?", Integer.class, recipientAccountId);
    }

    private Map<String, String> signedCallback(String merchantTxnRef, String amount, String responseCode) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Amount", amount);
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TmnCode", "DEMO");
        params.put("vnp_TransactionNo", "14123456");
        params.put("vnp_TransactionStatus", responseCode);
        params.put("vnp_TxnRef", merchantTxnRef);
        params.put("vnp_SecureHash", VnpaySigner.hmacSha512("DEMO_SECRET", VnpaySigner.canonicalize(params)));
        return params;
    }

    private Map<String, String> parseQuery(String url) {
        Map<String, String> query = new LinkedHashMap<>();
        String rawQuery = URI.create(url).getRawQuery();
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 1 ? "" : URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            query.put(key, value);
        }
        return query;
    }

    private Authentication customerAuth() {
        return auth(CUSTOMER_ID, "CUSTOMER", true);
    }

    private Authentication otherCustomerAuth() {
        return auth(OTHER_CUSTOMER_ID, "CUSTOMER", true);
    }

    private Authentication ownerAuth() {
        return auth(OWNER_ID, "OWNER", true);
    }

    private Authentication receptionistAuth() {
        return auth(RECEPTIONIST_ID, "RECEPTIONIST", true);
    }

    private Authentication auth(UUID accountId, String role, boolean verified) {
        AccountAuthUser principal = AccountAuthUser.builder()
                .accountId(accountId)
                .email(accountId + "@example.com")
                .passwordHash("hash")
                .emailVerified(verified)
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TransactionalTestConfig {
    }
}
