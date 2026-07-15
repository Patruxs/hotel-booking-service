package org.example.hotelbookingservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.example.hotelbookingservice.services.IBookingService;
import org.example.hotelbookingservice.services.IUserService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;
    private final IUserService userService;
    private final BookingCodeGenerator bookingCodeGenerator;
    private final BookingRoomRepository bookingRoomRepository;
    private final GuestDetailRepository guestDetailRepository;
    private final PhysicalRoomRepository physicalRoomRepository;


    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return bookingMapper.toBookingResponseList(bookingList);
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest bookingRequest) {

        validateBookingRequest(bookingRequest);

        User currentUser = userService.getCurrentLoggedInUser();

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_ROOM));

        if (!room.getHotel().getId().equals(bookingRequest.getHotelId())) {
            throw new AppException(ErrorCode.ROOM_NOT_BELONG_TO_HOTEL);
        }

        Long currentBookingsCount = bookingRepository.countBookedRooms(
                room.getId(),
                bookingRequest.getCheckinDate(),
                bookingRequest.getCheckoutDate()
        );
        long totalPhysicalRooms = physicalRoomRepository.countByRoom_IdAndActiveTrue(room.getUuid());

        int quantityToBook = bookingRequest.getRoomQuantity() != null ? bookingRequest.getRoomQuantity() : 1;

        if (currentBookingsCount + quantityToBook > totalPhysicalRooms) {
            throw new InvalidBookingStateAndDateException(
                    "Room is fully booked for the selected dates. Only "
                            + Math.max(0, totalPhysicalRooms - currentBookingsCount) + " rooms left.");
        }

        //calculate the total price needed to pay for the stay
        BigDecimal totalPrice = calculateTotalPrice(room, bookingRequest);
        String bookingReference = bookingCodeGenerator.generateBookingReference();

        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setCheckinDate(bookingRequest.getCheckinDate());
        booking.setCheckoutDate(bookingRequest.getCheckoutDate());
        booking.setAdultAmount(bookingRequest.getAdultAmount());
        booking.setChildrenAmount(bookingRequest.getChildrenAmount());
        booking.setCustomerName(currentUser.getFullName());
        booking.setTotalPrice(totalPrice.floatValue());
        booking.setBookingReference(bookingReference);
        booking.setStatus(BookingStatus.BOOKED);
        booking.setCreateAt(LocalDate.now());
        booking.setSpecialRequire(bookingRequest.getSpecialRequire());

        // Create BookingRoom and link instantly
        Bookingroom bookingRoom = new Bookingroom();
        bookingRoom.setBooking(booking); // Link from BookingRoom -> Booking
        bookingRoom.setRoom(room);

        // Link Booking -> BookingRoom (IMPORTANT FOR CASCADE TO WORK)
        // Entity Booking needs to initialize Set<Bookingroom> first (already done in Entity)
        booking.getBookingrooms().add(bookingRoom);

        // Save GuestDetails if provided
        if (bookingRequest.getGuestDetails() != null && !bookingRequest.getGuestDetails().isEmpty()) {
            for (GuestDetailRequest guestReq : bookingRequest.getGuestDetails()) {
                GuestDetail guestDetail = new GuestDetail();
                guestDetail.setFullName(guestReq.getFullName());
                guestDetail.setIdentityNumber(guestReq.getIdentityNumber());
                guestDetail.setBooking(booking);
                booking.getGuestDetails().add(guestDetail);
            }
        }

        // Hibernate will automatically save Booking -> get ID -> save BookingRoom + GuestDetails (cascade)
        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse findBookingByReferenceNo(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking with reference No: " + bookingReference + "Not found"));

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Integer bookingId, BookingUpdateRequest bookingRequest) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking Not Found"));

        // Handle status update
        if (bookingRequest.getStatus() != null) {
            BookingStatus newStatus = bookingRequest.getStatus();

            // === CHECKOUT LOGIC ===
            if (newStatus == BookingStatus.CHECKED_OUT) {
                // Add damageFee to totalPrice if present
                if (bookingRequest.getDamageFee() != null && bookingRequest.getDamageFee() > 0) {
                    existingBooking.setDamageFee(bookingRequest.getDamageFee());
                    existingBooking.setDamageDescription(bookingRequest.getDamageDescription());

                    // Add damageFee to totalPrice
                    float newTotal = existingBooking.getTotalPrice() + bookingRequest.getDamageFee();
                    existingBooking.setTotalPrice(newTotal);

                    log.info("Booking {} - Added damage fee: {}. New total: {}",
                            bookingId, bookingRequest.getDamageFee(), newTotal);
                }

                // Update PhysicalRoom status to DIRTY
                if (existingBooking.getRoomNumber() != null && !existingBooking.getRoomNumber().isBlank()) {
                    String roomNumber = existingBooking.getRoomNumber();
                    physicalRoomRepository.findByRoomNumber(roomNumber)
                            .ifPresent(physicalRoom -> {
                                physicalRoom.setRoomCondition(RoomCondition.DIRTY);
                                physicalRoomRepository.save(physicalRoom);
                                log.info("Physical room {} marked as DIRTY after checkout", roomNumber);
                            });
                }
            }

            existingBooking.setStatus(newStatus);
        }

        // Handle cancel reason
        if (bookingRequest.getCancelReason() != null) {
            existingBooking.setCancelReason(bookingRequest.getCancelReason());
        }

        // Handle damage fields directly (for cases without status change)
        if (bookingRequest.getDamageFee() != null && bookingRequest.getStatus() == null) {
            existingBooking.setDamageFee(bookingRequest.getDamageFee());
            existingBooking.setDamageDescription(bookingRequest.getDamageDescription());
        }

        // Logic check duplicate room
        if (bookingRequest.getRoomNumber() != null && !bookingRequest.getRoomNumber().isBlank()) {
            boolean isOccupied = bookingRepository.isRoomOccupied(
                    bookingRequest.getRoomNumber(),
                    bookingId
            );
            if (isOccupied) {
                throw new AppException(ErrorCode.ROOM_NUMBER_OCCUPIED);
            }
            existingBooking.setRoomNumber(bookingRequest.getRoomNumber());
        }

        Booking savedBooking = bookingRepository.save(existingBooking);
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Integer bookingId, String cancelReason) {

        //1. Find booking id
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking Not Found"));

        //2.get user login
        User currentUser = userService.getCurrentLoggedInUser();

        // 3. SECURITY CHECK
        //Allow cancellation if ADMIN OR owner (User ID matches)
        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName().equals(org.example.hotelbookingservice.enums.UserRole.ADMIN.name()));

        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 4. VALIDATION:
        // Completed or canceled orders cannot be canceled.
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new InvalidBookingStateAndDateException("Cannot cancel a booking that has already been completed.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateAndDateException("Booking is already cancelled.");
        }

        // 5. Execute cancellation
        booking.setStatus(BookingStatus.CANCELLED);

        if (cancelReason != null && !cancelReason.isBlank()) {
            booking.setCancelReason(cancelReason);
        } else {
            booking.setCancelReason("Cancelled by user (No reason provided)");
        }

        bookingRepository.save(booking);
    }

    private BigDecimal calculateTotalPrice(Room room, BookingCreateRequest bookingCreateRequest) {
        BigDecimal pricePerNight = room.getPrice();
        long days = ChronoUnit.DAYS.between(bookingCreateRequest.getCheckinDate(), bookingCreateRequest.getCheckoutDate());
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    private void validateBookingRequest(BookingCreateRequest request) {
        //validation: Ensure the check-in date is not before today
        if (request.getCheckinDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingStateAndDateException("Check-in date cannot be in the past");
        }
        //validation: Ensure the check-out date is not before check in date
        if (request.getCheckoutDate().isBefore(request.getCheckinDate())) {
            throw new InvalidBookingStateAndDateException("Check-out date cannot be before check-in date");
        }
        //validation: Ensure the check-in date is not same as check out date
        if (request.getCheckinDate().isEqual(request.getCheckoutDate())) {
            throw new InvalidBookingStateAndDateException("Check-in date cannot be same as check-out date");
        }
    }

    private UUID legacyId(Integer id) {
        return id == null ? null : new UUID(0L, id.longValue());
    }
}
