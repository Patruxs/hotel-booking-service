package org.example.hotelbookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.exception.GlobalExceptionHandler;
import org.example.hotelbookingservice.services.IBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private IBookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllBookings_WhenAdmin_ShouldReturn200AndList() throws Exception {
        // Arrange
        BookingResponse booking1 = new BookingResponse();
        booking1.setId(1);
        booking1.setBookingReference("CONFIRM1");

        BookingResponse booking2 = new BookingResponse();
        booking2.setId(2);
        booking2.setBookingReference("CONFIRM2");

        List<BookingResponse> mockResponse = List.of(booking1, booking2);
        when(bookingService.getAllBookings()).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/bookings/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].bookingReference").value("CONFIRM1"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].bookingReference").value("CONFIRM2"));
    }

    @Test
    void getAllBookings_WhenCustomer_ShouldReturn403() throws Exception {
        // Mock the service to throw AccessDeniedException
        doThrow(new AccessDeniedException("Access Denied"))
            .when(bookingService).getAllBookings();

        // Act & Assert
        mockMvc.perform(get("/api/v1/bookings/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(1007))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    void updateBooking_WhenCustomer_ShouldReturn403() throws Exception {
        // Arrange
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStatus(BookingStatus.CHECKED_IN);
        request.setRoomNumber("205");

        doThrow(new AccessDeniedException("Access Denied"))
            .when(bookingService).updateBooking(eq(1), any(BookingUpdateRequest.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/bookings/update/{bookingId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(1007))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    void createBooking_WhenValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCheckinDate(LocalDate.now().plusDays(1));
        request.setCheckoutDate(LocalDate.now().plusDays(3));
        request.setAdultAmount(2);
        request.setChildrenAmount(1);
        request.setHotelId(1);
        request.setRoomId(5);
        request.setRoomQuantity(1);
        request.setSpecialRequire("Quiet room");

        BookingResponse response = new BookingResponse();
        response.setId(10);
        response.setBookingReference("NEWBOOKING");

        when(bookingService.createBooking(any(BookingCreateRequest.class))).thenReturn(response);

        // Act & Assert
        // Dựa vào controller trả về new ResponseEntity và status 200, nhưng `ApiResponse` code = 201.
        mockMvc.perform(post("/api/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Booking successful"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.bookingReference").value("NEWBOOKING"));
    }

    @Test
    void createBooking_WhenCustomer_ShouldReturn403() throws Exception {
        // Arrange
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCheckinDate(LocalDate.now().plusDays(1));
        request.setCheckoutDate(LocalDate.now().plusDays(3));
        request.setAdultAmount(2);
        request.setChildrenAmount(1);
        request.setHotelId(1);
        request.setRoomId(5);
        request.setRoomQuantity(1);
        request.setSpecialRequire("Quiet room");

        doThrow(new AccessDeniedException("Access Denied"))
            .when(bookingService).createBooking(any(BookingCreateRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(1007))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    void createBooking_WhenRoomNotFound_ShouldReturn404() throws Exception {
        // Arrange
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCheckinDate(LocalDate.now().plusDays(1));
        request.setCheckoutDate(LocalDate.now().plusDays(3));
        request.setAdultAmount(2);
        request.setChildrenAmount(1);
        request.setHotelId(1);
        request.setRoomId(999); // Room doesn't exist
        request.setRoomQuantity(1);

        doThrow(new org.example.hotelbookingservice.exception.AppException(org.example.hotelbookingservice.exception.ErrorCode.NOT_FOUND_ROOM))
            .when(bookingService).createBooking(any(BookingCreateRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(10015))
                .andExpect(jsonPath("$.message").value("Not found Room"));
    }

    @Test
    void createBooking_WhenInvalidBookingState_ShouldReturn400() throws Exception {
        // Arrange
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCheckinDate(LocalDate.now().plusDays(1));
        request.setCheckoutDate(LocalDate.now().plusDays(3));
        request.setAdultAmount(2);
        request.setChildrenAmount(1);
        request.setHotelId(1);
        request.setRoomId(5);
        request.setRoomQuantity(1);

        doThrow(new org.example.hotelbookingservice.exception.InvalidBookingStateAndDateException("Room is fully booked"))
            .when(bookingService).createBooking(any(BookingCreateRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(1009))
                .andExpect(jsonPath("$.message").value("Room is fully booked"));
    }

    @Test
    void createBooking_WhenInvalidRequest_ShouldReturn400() throws Exception {
        // Arrange
        BookingCreateRequest request = new BookingCreateRequest();
        // Missing required fields like hotelId, roomId, etc.
        // Setting invalid dates
        request.setCheckinDate(LocalDate.now().minusDays(1)); // Past date
        request.setCheckoutDate(LocalDate.now().minusDays(2)); // Past date
        request.setAdultAmount(0); // Invalid, min is 1

        // Act & Assert
        mockMvc.perform(post("/api/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getBookingByConfirmationCode_ShouldReturn200() throws Exception {
        // Arrange
        BookingResponse response = new BookingResponse();
        response.setId(1);
        response.setBookingReference("CONFIRM123");

        when(bookingService.findBookingByReferenceNo("CONFIRM123")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/bookings/get-by-confirmation-code/{confirmationCode}", "CONFIRM123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.bookingReference").value("CONFIRM123"));
    }

    @Test
    void updateBooking_ShouldReturn200() throws Exception {
        // Arrange
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStatus(BookingStatus.CHECKED_IN);
        request.setRoomNumber("205");

        BookingResponse response = new BookingResponse();
        response.setId(1);
        response.setStatus(BookingStatus.CHECKED_IN);
        response.setRoomNumber("205");

        when(bookingService.updateBooking(eq(1), any(BookingUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/bookings/update/{bookingId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Booking Updated Successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.data.roomNumber").value("205"));
    }

    @Test
    void cancelBooking_ShouldReturn200() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/bookings/cancel/{bookingId}", 1)
                .param("reason", "Changed mind")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Booking cancelled successfully"));

        verify(bookingService, times(1)).cancelBooking(eq(1), eq("Changed mind"));
    }
}
