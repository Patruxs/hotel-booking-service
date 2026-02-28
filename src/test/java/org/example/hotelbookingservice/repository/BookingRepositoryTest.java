package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private Hotel testHotel;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // 1. Create User
        testUser = new User();
        testUser.setFullName("John Doe");
        testUser.setPassword("password123");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhone("1234567890");
        testUser.setDob(LocalDate.of(1990, 1, 1));
        testUser.setActivate(true);
        // Table name is properly escaped in Entity, so persisting should work if schema matches
        entityManager.persist(testUser);

        // 2. Create Hotel
        testHotel = new Hotel();
        testHotel.setName("Grand Hotel");
        testHotel.setLocation("New York");
        testHotel.setDescription("A very grand hotel");
        testHotel.setStarRating(5);
        testHotel.setEmail("grandhotel@example.com");
        testHotel.setPhone("0987654321");
        testHotel.setIsActive(true);
        testHotel.setContactName("Admin");
        testHotel.setContactPhone("111222333");
        testHotel.setUser(testUser);
        entityManager.persist(testHotel);

        // 3. Create Room
        testRoom = new Room();
        testRoom.setName("Ocean View 101");
        testRoom.setCapacity(2);
        testRoom.setPrice(BigDecimal.valueOf(150.0));
        testRoom.setDescription("Beautiful room with ocean view");
        testRoom.setType(RoomType.DOUBLE);
        testRoom.setAmount(10);
        testRoom.setHotel(testHotel);
        entityManager.persist(testRoom);

        entityManager.flush();
    }

    private Booking createBooking(LocalDate checkin, LocalDate checkout, BookingStatus status, float totalPrice) {
        Booking booking = new Booking();
        booking.setBookingReference("REF-" + System.currentTimeMillis());
        booking.setCheckinDate(checkin);
        booking.setCheckoutDate(checkout);
        booking.setAdultAmount(2);
        booking.setCustomerName("John Doe");
        booking.setStatus(status);
        booking.setCreateAt(LocalDate.now());
        booking.setChildrenAmount(0);
        booking.setTotalPrice(totalPrice);
        booking.setUser(testUser);
        entityManager.persist(booking);

        Bookingroom br = new Bookingroom();
        br.setBooking(booking);
        br.setRoom(testRoom);
        entityManager.persist(br);

        return booking;
    }

    @Test
    void isRoomAvailable_ShouldReturnTrue_WhenNoBookingOverlaps() {
        // Arrange: Room booked from 10th to 15th
        createBooking(LocalDate.of(2023, 10, 10), LocalDate.of(2023, 10, 15), BookingStatus.BOOKED, 500f);
        entityManager.flush();

        // Act: Check availability from 16th to 20th
        boolean isAvailable = bookingRepository.isRoomAvailable(
                testRoom.getId().longValue(),
                LocalDate.of(2023, 10, 16),
                LocalDate.of(2023, 10, 20)
        );

        // Assert
        assertThat(isAvailable).isTrue();
    }

    @Test
    void isRoomAvailable_ShouldReturnFalse_WhenBookingOverlaps() {
        // Arrange: Room booked from 10th to 15th
        createBooking(LocalDate.of(2023, 10, 10), LocalDate.of(2023, 10, 15), BookingStatus.BOOKED, 500f);
        entityManager.flush();

        // Act: Check availability from 12th to 18th (Overlaps)
        boolean isAvailable = bookingRepository.isRoomAvailable(
                testRoom.getId().longValue(),
                LocalDate.of(2023, 10, 12),
                LocalDate.of(2023, 10, 18)
        );

        // Assert
        assertThat(isAvailable).isFalse();
    }

    @Test
    void countBookedRooms_ShouldReturnCorrectCount() {
        // Arrange
        createBooking(LocalDate.of(2023, 11, 1), LocalDate.of(2023, 11, 5), BookingStatus.CHECKED_IN, 400f);
        createBooking(LocalDate.of(2023, 11, 4), LocalDate.of(2023, 11, 10), BookingStatus.BOOKED, 600f); // Overlaps
        createBooking(LocalDate.of(2023, 11, 20), LocalDate.of(2023, 11, 25), BookingStatus.BOOKED, 500f); // Non-overlapping
        entityManager.flush();

        // Act
        Long count = bookingRepository.countBookedRooms(
                testRoom.getId(),
                LocalDate.of(2023, 11, 2),
                LocalDate.of(2023, 11, 8)
        );

        // Assert: 2 bookings overlap with Nov 2 - Nov 8 (Nov 1-5, Nov 4-10)
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void getRevenueStatistics_ShouldReturnCorrectStats() {
        // Arrange
        // Booking 1: Checkout in Jan 2023, CHECKED_OUT, 1000
        createBooking(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 15), BookingStatus.CHECKED_OUT, 1000f);
        // Booking 2: Checkout in Jan 2023, CHECKED_OUT, 500
        createBooking(LocalDate.of(2023, 1, 20), LocalDate.of(2023, 1, 25), BookingStatus.CHECKED_OUT, 500f);
        // Booking 3: Checkout in Feb 2023, CHECKED_OUT, 800
        createBooking(LocalDate.of(2023, 2, 5), LocalDate.of(2023, 2, 10), BookingStatus.CHECKED_OUT, 800f);
        // Booking 4: Checkout in Jan 2023, CANCELLED, shouldn't be counted
        createBooking(LocalDate.of(2023, 1, 12), LocalDate.of(2023, 1, 14), BookingStatus.CANCELLED, 300f);
        // Booking 5: Checkout in Jan 2022, CHECKED_OUT, shouldn't be counted for 2023
        createBooking(LocalDate.of(2022, 1, 10), LocalDate.of(2022, 1, 15), BookingStatus.CHECKED_OUT, 2000f);
        entityManager.flush();

        // Act
        List<Object[]> stats = bookingRepository.getRevenueStatistics(2023);

        // Assert
        assertThat(stats).hasSize(2); // Jan and Feb

        // Find Jan stats
        Object[] janStats = stats.stream().filter(s -> (Integer) s[2] == 1).findFirst().orElseThrow();
        assertThat(janStats[0]).isEqualTo(testHotel.getId());
        assertThat(janStats[1]).isEqualTo("Grand Hotel");
        assertThat(janStats[2]).isEqualTo(1); // Month (Jan)
        assertThat(janStats[3]).isEqualTo(2L); // Count of bookings
        assertThat(((Number) janStats[4]).floatValue()).isEqualTo(1500f); // Sum of total price

        // Find Feb stats
        Object[] febStats = stats.stream().filter(s -> (Integer) s[2] == 2).findFirst().orElseThrow();
        assertThat(febStats[2]).isEqualTo(2); // Month (Feb)
        assertThat(febStats[3]).isEqualTo(1L); // Count of bookings
        assertThat(((Number) febStats[4]).floatValue()).isEqualTo(800f); // Sum of total price
    }

    @Test
    void isRoomOccupied_ShouldReturnTrue_WhenRoomIsCheckedIn() {
        // Arrange
        Booking booking = createBooking(LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), BookingStatus.CHECKED_IN, 500f);
        booking.setRoomNumber("101");
        entityManager.persist(booking);
        entityManager.flush();

        // Act
        boolean isOccupied = bookingRepository.isRoomOccupied("101", 9999); // 9999 is a fake currentBookingId

        // Assert
        assertThat(isOccupied).isTrue();
    }

    @Test
    void isRoomOccupied_ShouldReturnFalse_WhenRoomIsCheckedOut() {
        // Arrange
        Booking booking = createBooking(LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), BookingStatus.CHECKED_OUT, 500f);
        booking.setRoomNumber("101");
        entityManager.persist(booking);
        entityManager.flush();

        // Act
        boolean isOccupied = bookingRepository.isRoomOccupied("101", 9999);

        // Assert
        assertThat(isOccupied).isFalse();
    }

    @Test
    void isRoomOccupied_ShouldExcludeCurrentBookingId() {
        // Arrange
        Booking booking = createBooking(LocalDate.now().minusDays(1), LocalDate.now().plusDays(2), BookingStatus.CHECKED_IN, 500f);
        booking.setRoomNumber("101");
        entityManager.persist(booking);
        entityManager.flush();

        // Act: Pass the SAME booking id to check
        boolean isOccupied = bookingRepository.isRoomOccupied("101", booking.getId());

        // Assert
        assertThat(isOccupied).isFalse();
    }

}
