package org.example.hotelbookingservice.repository;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentCommercialJpaMappingTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("70000000-0000-4000-8000-000000000001");
    private static final UUID HOTEL_ID = UUID.fromString("70000000-0000-4000-8000-000000000002");
    private static final UUID ROOM_TYPE_ID = UUID.fromString("70000000-0000-4000-8000-000000000003");
    private static final UUID PROMOTION_ID = UUID.fromString("70000000-0000-4000-8000-000000000004");
    private static final UUID BOOKING_ID = UUID.fromString("70000000-0000-4000-8000-000000000005");
    private static final UUID PAYMENT_ID = UUID.fromString("70000000-0000-4000-8000-000000000006");
    private static final UUID PAYMENT_EVENT_ID = UUID.fromString("70000000-0000-4000-8000-000000000007");
    private static final UUID COMMISSION_PACKAGE_ID = UUID.fromString("70000000-0000-4000-8000-000000000008");

    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired PaymentEventRepository paymentEventRepository;
    @Autowired CommissionPackageRepository commissionPackageRepository;
    @Autowired HotelCommissionPackageRepository hotelCommissionPackageRepository;
    @Autowired PromotionRepository promotionRepository;

    @Test
    void paymentCommercialEntitiesLoadAgainstFlywaySchema() {
        insertParents();
        insertCommercialRows();
        insertPaymentRows();

        var paymentEvent = paymentEventRepository.findByPaymentIdOrderByCreatedAt(PAYMENT_ID).getFirst();
        var commissionPackage = commissionPackageRepository.findById(COMMISSION_PACKAGE_ID).orElseThrow();
        var assignment = hotelCommissionPackageRepository.findWithPackageByHotelId(HOTEL_ID).orElseThrow();
        var promotion = promotionRepository.findById(PROMOTION_ID).orElseThrow();
        var persistenceUnit = entityManagerFactory.getPersistenceUnitUtil();

        assertThat(paymentEvent.getId()).isEqualTo(PAYMENT_EVENT_ID);
        assertThat(paymentEvent.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(paymentEvent.getEventType()).isEqualTo("VNPAY_RETURN");
        assertThat(paymentEvent.getPayload()).containsEntry("vnp_ResponseCode", "00");
        assertThat(persistenceUnit.isLoaded(paymentEvent, "payment")).isTrue();

        assertThat(commissionPackage.getCode()).isEqualTo("MAPPING");
        assertThat(commissionPackage.getDescription()).isEqualTo("Mapping package");
        assertThat(commissionPackage.isActive()).isTrue();
        assertThat(assignment.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(assignment.getCommissionPackageId()).isEqualTo(COMMISSION_PACKAGE_ID);
        assertThat(assignment.getCommissionPackage().getCode()).isEqualTo("MAPPING");

        assertThat(promotion.getCode()).isEqualTo("MAP10");
        assertThat(promotion.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(promotion.getDiscountType()).isEqualTo("PERCENT");
        assertThat(promotion.getDiscountValue()).isEqualByComparingTo("10.00");
    }

    @Test
    void promotionAndCommissionRepositoryMethodsUseCurrentTables() {
        insertParents();
        insertCommercialRows();

        var promotion = promotionRepository.findActiveForBooking(HOTEL_ID, "map10", BigDecimal.valueOf(200), Instant.now()).orElseThrow();
        var commission = commissionPackageRepository.findCommissionForHotel(HOTEL_ID).orElseThrow();
        int consumed = promotionRepository.incrementUsedCount(PROMOTION_ID, Instant.now());
        int restored = promotionRepository.decrementUsedCount(PROMOTION_ID, Instant.now());

        assertThat(promotion.getId()).isEqualTo(PROMOTION_ID);
        assertThat(commission.getCode()).isEqualTo("MAPPING");
        assertThat(commission.getCommissionRate()).isEqualByComparingTo("0.1234");
        assertThat(consumed).isEqualTo(1);
        assertThat(restored).isEqualTo(1);
        assertThat(jdbc.queryForObject("select used_count from promotions where id = ?", Integer.class, PROMOTION_ID)).isZero();
    }

    private void insertParents() {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified)
                values (?, 'payment-commercial-mapping@example.com', 'hash', 'Payment', 'Commercial', true)
                on conflict (id) do nothing
                """, ACCOUNT_ID);
        jdbc.update("""
                insert into hotels (id, owner_id, name, slug, status)
                values (?, ?, 'Payment Commercial Hotel', 'payment-commercial-hotel', 'ACTIVE')
                on conflict (id) do nothing
                """, HOTEL_ID, ACCOUNT_ID);
        jdbc.update("""
                insert into room_types (id, hotel_id, name, price_per_night, max_guests)
                values (?, ?, 'Commercial Deluxe', ?, 2)
                on conflict (id) do nothing
                """, ROOM_TYPE_ID, HOTEL_ID, BigDecimal.valueOf(100));
    }

    private void insertCommercialRows() {
        jdbc.update("""
                insert into commission_packages (id, code, name, description, commission_rate, active, is_system)
                values (?, 'MAPPING', 'Mapping', 'Mapping package', 0.1234, true, false)
                on conflict (id) do nothing
                """, COMMISSION_PACKAGE_ID);
        jdbc.update("""
                insert into hotel_commission_packages (hotel_id, commission_package_id)
                values (?, ?)
                on conflict (hotel_id) do update set commission_package_id = excluded.commission_package_id
                """, HOTEL_ID, COMMISSION_PACKAGE_ID);
        jdbc.update("""
                insert into promotions (
                    id, hotel_id, code, name, discount_type, discount_value, min_booking_amount, used_count, active
                )
                values (?, ?, 'MAP10', 'Mapping Promo', 'PERCENT', 10.00, 100.00, 0, true)
                on conflict (id) do nothing
                """, PROMOTION_ID, HOTEL_ID);
    }

    private void insertPaymentRows() {
        LocalDate checkIn = LocalDate.of(2027, 8, 1);
        jdbc.update("""
                insert into bookings (
                    id, account_id, hotel_id, promotion_id, booking_reference, status, check_in, check_out,
                    guest_name, guest_email, guest_phone, subtotal_amount, discount_amount, total_amount
                )
                values (?, ?, ?, ?, 'PAY-COM-001', 'CONFIRMED', ?, ?, 'Payment Guest',
                        'payment-guest@example.com', '0900000000', 100.00, 10.00, 90.00)
                on conflict (id) do nothing
                """, BOOKING_ID, ACCOUNT_ID, HOTEL_ID, PROMOTION_ID, checkIn, checkIn.plusDays(1));
        jdbc.update("""
                insert into payments (id, booking_id, provider, status, amount, currency, merchant_txn_ref)
                values (?, ?, 'VNPAY', 'PENDING', 90.00, 'VND', 'PAY-COM-001')
                on conflict (id) do nothing
                """, PAYMENT_ID, BOOKING_ID);
        paymentEventRepository.insertEvent(PAYMENT_EVENT_ID, PAYMENT_ID, "VNPAY_RETURN", "{\"vnp_ResponseCode\":\"00\"}");
    }
}
