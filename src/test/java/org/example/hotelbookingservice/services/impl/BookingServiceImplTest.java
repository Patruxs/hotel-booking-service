package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.request.booking.GuestDetailRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.enums.RoomCondition;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;
import org.example.hotelbookingservice.exception.NotFoundException;
import org.example.hotelbookingservice.mapper.BookingMapper;
import org.example.hotelbookingservice.repository.BookingRepository;
import org.example.hotelbookingservice.repository.BookingRoomRepository;
import org.example.hotelbookingservice.repository.GuestDetailRepository;
import org.example.hotelbookingservice.repository.PhysicalRoomRepository;
import org.example.hotelbookingservice.repository.RoomRepository;
import org.example.hotelbookingservice.services.BookingCodeGenerator;
import org.example.hotelbookingservice.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private IUserService userService;

    @Mock
    private BookingCodeGenerator bookingCodeGenerator;

    @Mock
    private BookingRoomRepository bookingRoomRepository;

    @Mock
    private GuestDetailRepository guestDetailRepository;

    @Mock
    private PhysicalRoomRepository physicalRoomRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User mockUser;
    private Hotel mockHotel;
    private Room mockRoom;
    private BookingCreateRequest mockCreateRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.fromString("10000000-0000-4000-8000-000000000001"));
        mockUser.setFullName("John Doe");

        mockHotel = new Hotel();
        mockHotel.setId(100);

        mockRoom = new Room();
        mockRoom.setId(10);
        mockRoom.setHotel(mockHotel);
        mockRoom.setAmount(5);
        mockRoom.setPrice(BigDecimal.valueOf(1000));

        mockCreateRequest = new BookingCreateRequest();
        mockCreateRequest.setHotelId(100);
        mockCreateRequest.setRoomId(10);
        mockCreateRequest.setCheckinDate(LocalDate.now().plusDays(1));
        mockCreateRequest.setCheckoutDate(LocalDate.now().plusDays(3));
        mockCreateRequest.setAdultAmount(2);
        mockCreateRequest.setChildrenAmount(1);
        mockCreateRequest.setRoomQuantity(2);
        mockCreateRequest.setSpecialRequire("No special request");
    }

    @Test
    void createBooking_ValidRequest_ReturnsBookingResponse() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(1L); // 1 room currently booked

        when(physicalRoomRepository.countByRoom_IdAndActiveTrue(mockRoom.getUuid())).thenReturn(5L);

        when(bookingCodeGenerator.generateBookingReference()).thenReturn("BOOK123");

        Booking mockSavedBooking = new Booking();
        mockSavedBooking.setId(1);
        mockSavedBooking.setBookingReference("BOOK123");
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockSavedBooking);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(1);
        mockResponse.setBookingReference("BOOK123");
        when(bookingMapper.toBookingResponse(mockSavedBooking)).thenReturn(mockResponse);

        // When
        BookingResponse result = bookingService.createBooking(mockCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo("BOOK123");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_CheckinDateInPast_ThrowsInvalidBookingStateAndDateException() {
        // Given
        mockCreateRequest.setCheckinDate(LocalDate.now().minusDays(1));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-in date cannot be in the past");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_CheckoutDateBeforeCheckin_ThrowsInvalidBookingStateAndDateException() {
        // Given
        mockCreateRequest.setCheckinDate(LocalDate.now().plusDays(2));
        mockCreateRequest.setCheckoutDate(LocalDate.now().plusDays(1));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-out date cannot be before check-in date");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_CheckoutDateSameAsCheckin_ThrowsInvalidBookingStateAndDateException() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        mockCreateRequest.setCheckinDate(date);
        mockCreateRequest.setCheckoutDate(date);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Check-in date cannot be same as check-out date");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_RoomNotFound_ThrowsAppException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_ROOM);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_RoomNotBelongToHotel_ThrowsAppException() {
        // Given
        mockCreateRequest.setHotelId(999); // Different hotel
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_NOT_BELONG_TO_HOTEL);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ExceedsAvailableRooms_ThrowsInvalidBookingStateAndDateException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        // 5 total rooms. Want to book 2.
        // Currently booked: 4. Total = 6 > 5.
        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(4L);
        when(physicalRoomRepository.countByRoom_IdAndActiveTrue(mockRoom.getUuid())).thenReturn(5L);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessageContaining("Room is fully booked for the selected dates. Only 1 rooms left.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_OverlappingBookingWithinPhysicalCapacity_SavesBooking() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(1L);
        when(physicalRoomRepository.countByRoom_IdAndActiveTrue(mockRoom.getUuid())).thenReturn(5L);
        when(bookingCodeGenerator.generateBookingReference()).thenReturn("BOOK-CAPACITY");

        Booking savedBooking = new Booking();
        savedBooking.setId(1);
        savedBooking.setBookingReference("BOOK-CAPACITY");
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponse response = new BookingResponse();
        response.setId(1);
        response.setBookingReference("BOOK-CAPACITY");
        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(response);

        // When
        BookingResponse result = bookingService.createBooking(mockCreateRequest);

        // Then
        assertThat(result.getBookingReference()).isEqualTo("BOOK-CAPACITY");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void cancelBooking_ByUserOwnBooking_Success() {
        // Given
        Integer bookingId = 1;
        String cancelReason = "Change of plans";

        org.example.hotelbookingservice.entity.UserRole userRole = new org.example.hotelbookingservice.entity.UserRole();
        Role role = new Role();
        role.setName("USER");
        userRole.setRole(role);
        mockUser.getUserRoles().add(userRole);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setUser(mockUser);
        existingBooking.setStatus(BookingStatus.BOOKED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);

        // When
        bookingService.cancelBooking(bookingId, cancelReason);

        // Then
        assertThat(existingBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(existingBooking.getCancelReason()).isEqualTo(cancelReason);
        verify(bookingRepository, times(1)).save(existingBooking);
    }

    @Test
    void cancelBooking_ByAdmin_Success() {
        // Given
        Integer bookingId = 1;
        String cancelReason = "";

        User adminUser = new User();
        adminUser.setId(UUID.fromString("10000000-0000-4000-8000-000000000999"));
        org.example.hotelbookingservice.entity.UserRole adminRole = new org.example.hotelbookingservice.entity.UserRole();
        Role role = new Role();
        role.setName("ADMIN");
        adminRole.setRole(role);
        adminUser.getUserRoles().add(adminRole);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setUser(mockUser); // Booking belongs to another user
        existingBooking.setStatus(BookingStatus.BOOKED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(userService.getCurrentLoggedInUser()).thenReturn(adminUser);

        // When
        bookingService.cancelBooking(bookingId, cancelReason);

        // Then
        assertThat(existingBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(existingBooking.getCancelReason()).isEqualTo("Cancelled by user (No reason provided)");
        verify(bookingRepository, times(1)).save(existingBooking);
    }

    @Test
    void cancelBooking_BookingNotFound_ThrowsNotFoundException() {
        // Given
        Integer bookingId = 1;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "Reason"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Booking Not Found");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_UserNotAuthorized_ThrowsAppException() {
        // Given
        Integer bookingId = 1;

        User otherUser = new User();
        otherUser.setId(UUID.fromString("10000000-0000-4000-8000-000000000999"));
        org.example.hotelbookingservice.entity.UserRole userRole = new org.example.hotelbookingservice.entity.UserRole();
        Role role = new Role();
        role.setName("USER");
        userRole.setRole(role);
        otherUser.getUserRoles().add(userRole);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setUser(mockUser); // Belongs to mockUser (ID 1)
        existingBooking.setStatus(BookingStatus.BOOKED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(userService.getCurrentLoggedInUser()).thenReturn(otherUser); // Logged in as otherUser (ID 999)

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "Reason"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_AlreadyCheckedOut_ThrowsInvalidBookingStateAndDateException() {
        // Given
        Integer bookingId = 1;

        org.example.hotelbookingservice.entity.UserRole userRole = new org.example.hotelbookingservice.entity.UserRole();
        Role role = new Role();
        role.setName("USER");
        userRole.setRole(role);
        mockUser.getUserRoles().add(userRole);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setUser(mockUser);
        existingBooking.setStatus(BookingStatus.CHECKED_OUT);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "Reason"))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Cannot cancel a booking that has already been completed.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_AlreadyCancelled_ThrowsInvalidBookingStateAndDateException() {
        // Given
        Integer bookingId = 1;

        org.example.hotelbookingservice.entity.UserRole userRole = new org.example.hotelbookingservice.entity.UserRole();
        Role role = new Role();
        role.setName("USER");
        userRole.setRole(role);
        mockUser.getUserRoles().add(userRole);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setUser(mockUser);
        existingBooking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "Reason"))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Booking is already cancelled.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getAllBookings_ReturnsBookingResponseList() {
        // Given
        Booking booking1 = new Booking();
        booking1.setId(1);
        Booking booking2 = new Booking();
        booking2.setId(2);
        List<Booking> bookings = List.of(booking1, booking2);

        BookingResponse response1 = new BookingResponse();
        response1.setId(1);
        BookingResponse response2 = new BookingResponse();
        response2.setId(2);
        List<BookingResponse> responses = List.of(response1, response2);

        when(bookingRepository.findAll(any(Sort.class))).thenReturn(bookings);
        when(bookingMapper.toBookingResponseList(bookings)).thenReturn(responses);

        // When
        List<BookingResponse> result = bookingService.getAllBookings();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(bookingRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    void findBookingByReferenceNo_ValidReference_ReturnsBookingResponse() {
        // Given
        String reference = "REF123";
        Booking booking = new Booking();
        booking.setBookingReference(reference);

        BookingResponse response = new BookingResponse();
        response.setBookingReference(reference);

        when(bookingRepository.findByBookingReference(reference)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponse(booking)).thenReturn(response);

        // When
        BookingResponse result = bookingService.findBookingByReferenceNo(reference);

        // Then
        assertThat(result.getBookingReference()).isEqualTo(reference);
        verify(bookingRepository, times(1)).findByBookingReference(reference);
    }

    @Test
    void findBookingByReferenceNo_NotFound_ThrowsNotFoundException() {
        // Given
        String reference = "INVALID_REF";
        when(bookingRepository.findByBookingReference(reference)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.findBookingByReferenceNo(reference))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Booking with reference No: " + reference + "Not found");
    }

    @Test
    void updateBooking_ValidRequest_ReturnsBookingResponse() {
        // Given
        Integer bookingId = 1;
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStatus(BookingStatus.CHECKED_IN);
        updateRequest.setCancelReason("Test update");
        updateRequest.setRoomNumber("101");

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStatus(BookingStatus.BOOKED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.isRoomOccupied(updateRequest.getRoomNumber(), bookingId)).thenReturn(false);

        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        savedBooking.setStatus(BookingStatus.CHECKED_IN);
        savedBooking.setCancelReason("Test update");
        savedBooking.setRoomNumber("101");

        when(bookingRepository.save(existingBooking)).thenReturn(savedBooking);

        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        response.setStatus(BookingStatus.CHECKED_IN);

        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(response);

        // When
        BookingResponse result = bookingService.updateBooking(bookingId, updateRequest);

        // Then
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CHECKED_IN);
        assertThat(existingBooking.getStatus()).isEqualTo(BookingStatus.CHECKED_IN);
        assertThat(existingBooking.getRoomNumber()).isEqualTo("101");
        verify(bookingRepository, times(1)).save(existingBooking);
    }

    @Test
    void updateBooking_RoomOccupied_ThrowsAppException() {
        // Given
        Integer bookingId = 1;
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setRoomNumber("101");

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.isRoomOccupied(updateRequest.getRoomNumber(), bookingId)).thenReturn(true); // Room is occupied

        // When & Then
        assertThatThrownBy(() -> bookingService.updateBooking(bookingId, updateRequest))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ROOM_NUMBER_OCCUPIED);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    // ======================== NEW TESTS: PHASE 3/4 FEATURES ========================

    @Test
    void updateBooking_CheckoutWithDamageFee_AddsDamageFeeToTotalPrice() {
        // Given
        Integer bookingId = 1;
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStatus(BookingStatus.CHECKED_OUT);
        updateRequest.setDamageFee(500000f);
        updateRequest.setDamageDescription("Broken mirror in bathroom");

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStatus(BookingStatus.CHECKED_IN);
        existingBooking.setTotalPrice(2000000f); // Original total
        existingBooking.setRoomNumber("301");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        // Mock: No physical room mapped to room number 301
        when(physicalRoomRepository.findByRoomNumber("301")).thenReturn(Optional.empty());

        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        savedBooking.setTotalPrice(2500000f);
        when(bookingRepository.save(existingBooking)).thenReturn(savedBooking);

        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        response.setTotalPrice(2500000f);
        response.setDamageFee(500000f);
        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(response);

        // When
        BookingResponse result = bookingService.updateBooking(bookingId, updateRequest);

        // Then
        assertThat(existingBooking.getTotalPrice()).isEqualTo(2500000f); // 2000000 + 500000
        assertThat(existingBooking.getDamageFee()).isEqualTo(500000f);
        assertThat(existingBooking.getDamageDescription()).isEqualTo("Broken mirror in bathroom");
        assertThat(existingBooking.getStatus()).isEqualTo(BookingStatus.CHECKED_OUT);
        verify(bookingRepository, times(1)).save(existingBooking);
    }

    @Test
    void updateBooking_CheckoutWithoutDamageFee_KeepsTotalPriceUnchanged() {
        // Given
        Integer bookingId = 1;
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStatus(BookingStatus.CHECKED_OUT);
        // No damageFee set

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStatus(BookingStatus.CHECKED_IN);
        existingBooking.setTotalPrice(2000000f);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));

        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        savedBooking.setTotalPrice(2000000f);
        when(bookingRepository.save(existingBooking)).thenReturn(savedBooking);

        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        response.setTotalPrice(2000000f);
        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(response);

        // When
        BookingResponse result = bookingService.updateBooking(bookingId, updateRequest);

        // Then
        assertThat(existingBooking.getTotalPrice()).isEqualTo(2000000f); // Unchanged
        assertThat(existingBooking.getDamageFee()).isNull();
        assertThat(existingBooking.getStatus()).isEqualTo(BookingStatus.CHECKED_OUT);
        verify(bookingRepository, times(1)).save(existingBooking);
    }

    @Test
    void updateBooking_CheckoutMarksPhysicalRoomAsDirty() {
        // Given
        Integer bookingId = 1;
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStatus(BookingStatus.CHECKED_OUT);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStatus(BookingStatus.CHECKED_IN);
        existingBooking.setTotalPrice(1000000f);
        existingBooking.setRoomNumber("205"); // Physical room number

        PhysicalRoom physicalRoom = new PhysicalRoom();
        physicalRoom.setId(5);
        physicalRoom.setRoomNumber("205");
        physicalRoom.setRoomCondition(RoomCondition.CLEAN);
        Room linkedRoom = new Room();
        linkedRoom.setId(10);
        linkedRoom.setName("Deluxe");
        physicalRoom.setRoom(linkedRoom);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(physicalRoomRepository.findByRoomNumber("205")).thenReturn(Optional.of(physicalRoom));

        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        when(bookingRepository.save(existingBooking)).thenReturn(savedBooking);

        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(response);

        // When
        bookingService.updateBooking(bookingId, updateRequest);

        // Then
        assertThat(physicalRoom.getRoomCondition()).isEqualTo(RoomCondition.DIRTY);
        verify(physicalRoomRepository, times(1)).save(physicalRoom);
        verify(physicalRoomRepository, times(1)).findByRoomNumber("205");
    }

    @Test
    void updateBooking_CheckoutMarksAlphanumericPhysicalRoomAsDirty() {
        // Given
        Integer bookingId = 1;
        BookingUpdateRequest updateRequest = new BookingUpdateRequest();
        updateRequest.setStatus(BookingStatus.CHECKED_OUT);

        Booking existingBooking = new Booking();
        existingBooking.setId(bookingId);
        existingBooking.setStatus(BookingStatus.CHECKED_IN);
        existingBooking.setTotalPrice(1000000f);
        existingBooking.setRoomNumber("D101");

        PhysicalRoom physicalRoom = new PhysicalRoom();
        physicalRoom.setId(5);
        physicalRoom.setRoomNumber("D101");
        physicalRoom.setRoomCondition(RoomCondition.CLEAN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(physicalRoomRepository.findByRoomNumber("D101")).thenReturn(Optional.of(physicalRoom));

        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        when(bookingRepository.save(existingBooking)).thenReturn(savedBooking);

        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(response);

        // When
        bookingService.updateBooking(bookingId, updateRequest);

        // Then
        assertThat(physicalRoom.getRoomCondition()).isEqualTo(RoomCondition.DIRTY);
        verify(physicalRoomRepository, times(1)).save(physicalRoom);
        verify(physicalRoomRepository, times(1)).findByRoomNumber("D101");
    }

    @Test
    void createBooking_WithGuestDetails_SavesGuestDetailsViaCascade() {
        // Given
        GuestDetailRequest guest1 = new GuestDetailRequest();
        guest1.setFullName("Nguyen Van A");
        guest1.setIdentityNumber("079203012345");

        GuestDetailRequest guest2 = new GuestDetailRequest();
        guest2.setFullName("Le Thi B");
        guest2.setIdentityNumber("079203067890");

        List<GuestDetailRequest> guests = new ArrayList<>();
        guests.add(guest1);
        guests.add(guest2);
        mockCreateRequest.setGuestDetails(guests);

        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));
        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(0L);
        when(physicalRoomRepository.countByRoom_IdAndActiveTrue(mockRoom.getUuid())).thenReturn(5L);
        when(bookingCodeGenerator.generateBookingReference()).thenReturn("GUEST123");

        Booking mockSavedBooking = new Booking();
        mockSavedBooking.setId(1);
        mockSavedBooking.setBookingReference("GUEST123");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            // Verify guest details were added to the booking before save
            assertThat(booking.getGuestDetails()).hasSize(2);
            return mockSavedBooking;
        });

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(1);
        mockResponse.setBookingReference("GUEST123");
        when(bookingMapper.toBookingResponse(mockSavedBooking)).thenReturn(mockResponse);

        // When
        BookingResponse result = bookingService.createBooking(mockCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo("GUEST123");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }
}
