package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.entity.*;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException;
import org.example.hotelbookingservice.exception.NotFoundException;
import org.example.hotelbookingservice.mapper.BookingMapper;
import org.example.hotelbookingservice.repository.BookingRepository;
import org.example.hotelbookingservice.repository.BookingRoomRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

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

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User mockUser;
    private Hotel mockHotel;
    private Room mockRoom;
    private BookingCreateRequest mockCreateRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
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

        when(bookingRepository.isRoomAvailable(
                Long.valueOf(mockRoom.getId()),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(true);

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
    void createBooking_NullRoomQuantity_ReturnsBookingResponse() {
        // Given
        mockCreateRequest.setRoomQuantity(null); // Explicitly set to null

        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        // 5 total rooms. default quantity = 1.
        // Currently booked: 1. Total = 2 <= 5.
        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(1L);

        when(bookingRepository.isRoomAvailable(
                Long.valueOf(mockRoom.getId()),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(true);

        when(bookingCodeGenerator.generateBookingReference()).thenReturn("BOOK_NULL_QTY");

        Booking mockSavedBooking = new Booking();
        mockSavedBooking.setId(3);
        mockSavedBooking.setBookingReference("BOOK_NULL_QTY");
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockSavedBooking);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(3);
        mockResponse.setBookingReference("BOOK_NULL_QTY");
        when(bookingMapper.toBookingResponse(mockSavedBooking)).thenReturn(mockResponse);

        // When
        BookingResponse result = bookingService.createBooking(mockCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo("BOOK_NULL_QTY");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_ExactBoundary_ReturnsBookingResponse() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        // 5 total rooms. Want to book 2.
        // Currently booked: 3. Total = 5 == 5. Boundary case should succeed.
        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(3L);

        when(bookingRepository.isRoomAvailable(
                Long.valueOf(mockRoom.getId()),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(true);

        when(bookingCodeGenerator.generateBookingReference()).thenReturn("BOOK_EXACT");

        Booking mockSavedBooking = new Booking();
        mockSavedBooking.setId(2);
        mockSavedBooking.setBookingReference("BOOK_EXACT");
        when(bookingRepository.save(any(Booking.class))).thenReturn(mockSavedBooking);

        BookingResponse mockResponse = new BookingResponse();
        mockResponse.setId(2);
        mockResponse.setBookingReference("BOOK_EXACT");
        when(bookingMapper.toBookingResponse(mockSavedBooking)).thenReturn(mockResponse);

        // When
        BookingResponse result = bookingService.createBooking(mockCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingReference()).isEqualTo("BOOK_EXACT");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_ZeroAvailableRooms_ThrowsInvalidBookingStateAndDateException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        // 5 total rooms. Want to book 2 (default from mockCreateRequest).
        // Currently booked: 5. Total = 7 > 5. Room is fully booked.
        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(5L);

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessageContaining("Room is fully booked for the selected dates. Only 0 rooms left.");

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

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessageContaining("Room is fully booked for the selected dates. Only 1 rooms left.");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_RoomNotAvailable_ThrowsInvalidBookingStateAndDateException() {
        // Given
        when(userService.getCurrentLoggedInUser()).thenReturn(mockUser);
        when(roomRepository.findById(mockCreateRequest.getRoomId())).thenReturn(Optional.of(mockRoom));

        when(bookingRepository.countBookedRooms(
                mockRoom.getId(),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(1L);

        when(bookingRepository.isRoomAvailable(
                Long.valueOf(mockRoom.getId()),
                mockCreateRequest.getCheckinDate(),
                mockCreateRequest.getCheckoutDate()
        )).thenReturn(false); // Room not available

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(mockCreateRequest))
                .isInstanceOf(InvalidBookingStateAndDateException.class)
                .hasMessage("Room is not available for the selected date ranges");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_ByUserOwnBooking_Success() {
        // Given
        Integer bookingId = 1;
        String cancelReason = "Change of plans";

        Userrole userRole = new Userrole();
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
        adminUser.setId(999);
        Userrole adminRole = new Userrole();
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
        otherUser.setId(999);
        Userrole userRole = new Userrole();
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

        Userrole userRole = new Userrole();
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

        Userrole userRole = new Userrole();
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
}
