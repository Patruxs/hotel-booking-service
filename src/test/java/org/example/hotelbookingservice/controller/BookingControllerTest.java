package org.example.hotelbookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.booking.BookingCreateRequest;
import org.example.hotelbookingservice.dto.request.booking.BookingUpdateRequest;
import org.example.hotelbookingservice.dto.response.BookingResponse;
import org.example.hotelbookingservice.enums.BookingStatus;
import org.example.hotelbookingservice.services.IBookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IBookingService bookingService;

    @MockitoBean
    private org.example.hotelbookingservice.security.JwtUtils jwtUtils;

    @MockitoBean
    private org.example.hotelbookingservice.security.CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private org.example.hotelbookingservice.exception.CustomAccessDenialHandler customAccessDenialHandler;

    @MockitoBean
    private org.example.hotelbookingservice.exception.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAllBookings_whenAdmin_shouldReturn200AndList() throws Exception {
        BookingResponse booking1 = new BookingResponse();
        booking1.setId(1);
        booking1.setBookingReference("CONFIRM1");

        BookingResponse booking2 = new BookingResponse();
        booking2.setId(2);
        booking2.setBookingReference("CONFIRM2");

        List<BookingResponse> mockResponse = List.of(booking1, booking2);
        when(bookingService.getAllBookings()).thenReturn(mockResponse);

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
    @WithMockUser(authorities = "CUSTOMER")
    void getAllBookings_whenServiceDeniesAccess_shouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Access Denied"))
            .when(bookingService).getAllBookings();

        mockMvc.perform(get("/api/v1/bookings/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void updateBooking_whenServiceDeniesAccess_shouldReturn403() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStatus(BookingStatus.CHECKED_IN);
        request.setRoomNumber("205");

        doThrow(new AccessDeniedException("Access Denied"))
            .when(bookingService).updateBooking(eq(1), any(BookingUpdateRequest.class));

        mockMvc.perform(put("/api/v1/bookings/update/{bookingId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("You do not have permission"));
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void createBooking_whenValidRequest_shouldReturn201Payload() throws Exception {
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
    @WithMockUser(authorities = "CUSTOMER")
    void createBooking_whenInvalidRequest_shouldReturn400() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setCheckinDate(LocalDate.now().minusDays(1));
        request.setCheckoutDate(LocalDate.now().minusDays(2));
        request.setAdultAmount(0);

        mockMvc.perform(post("/api/v1/bookings/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.violations").exists());
    }

    @Test
    void getBookingByConfirmationCode_whenFound_shouldReturn200() throws Exception {
        BookingResponse response = new BookingResponse();
        response.setId(1);
        response.setBookingReference("CONFIRM123");

        when(bookingService.findBookingByReferenceNo("CONFIRM123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/bookings/get-by-confirmation-code/{confirmationCode}", "CONFIRM123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.bookingReference").value("CONFIRM123"));
    }

    @Test
    @WithMockUser(authorities = "RECEPTIONIST")
    void updateBooking_whenValidRequest_shouldReturn200() throws Exception {
        BookingUpdateRequest request = new BookingUpdateRequest();
        request.setStatus(BookingStatus.CHECKED_IN);
        request.setRoomNumber("205");

        BookingResponse response = new BookingResponse();
        response.setId(1);
        response.setStatus(BookingStatus.CHECKED_IN);
        response.setRoomNumber("205");

        when(bookingService.updateBooking(eq(1), any(BookingUpdateRequest.class))).thenReturn(response);

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
    @WithMockUser(authorities = "CUSTOMER")
    void cancelBooking_whenRequestHasReason_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/v1/bookings/cancel/{bookingId}", 1)
                .param("reason", "Changed mind")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Booking cancelled successfully"));

        verify(bookingService, times(1)).cancelBooking(eq(1), eq("Changed mind"));
    }
}
