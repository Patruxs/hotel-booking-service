package org.example.hotelbookingservice.controller;

import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.hotel.HotelCreateRequest;
import org.example.hotelbookingservice.dto.response.HotelResponse;
import org.example.hotelbookingservice.exception.AppException;
import org.example.hotelbookingservice.exception.ErrorCode;
import org.example.hotelbookingservice.exception.GlobalExceptionHandler;
import org.example.hotelbookingservice.services.IHotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class HotelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IHotelService hotelService;

    @InjectMocks
    private HotelController hotelController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hotelController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addHotel_WhenValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "hotel.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes());

        HotelResponse hotelResponse = new HotelResponse();
        hotelResponse.setId(1);
        hotelResponse.setName("Grand Saigon Hotel");

        when(hotelService.addHotel(any(HotelCreateRequest.class), anyList()))
                .thenReturn(hotelResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/hotels/add")
                .file(imageFile)
                .param("name", "Grand Saigon Hotel")
                .param("location", "Ho Chi Minh City")
                .param("description", "Khách sạn 5 sao trung tâm thành phố...")
                .param("starRating", "5")
                .param("email", "contact@grandsaigon.com")
                .param("phone", "02839123456")
                .param("contactName", "Mr. Quan Ly")
                .param("contactPhone", "0909123456")
                .param("isActive", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Hotel added successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Grand Saigon Hotel"));

        verify(hotelService, times(1)).addHotel(any(HotelCreateRequest.class), anyList());
    }

    @Test
    void addHotel_WhenCustomer_ShouldReturn403() throws Exception {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "hotel.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes());

        when(hotelService.addHotel(any(HotelCreateRequest.class), anyList()))
                .thenThrow(new AccessDeniedException("Access Denied"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/hotels/add")
                .file(imageFile)
                .param("name", "Grand Saigon Hotel")
                .param("location", "Ho Chi Minh City")
                .param("description", "Khách sạn 5 sao trung tâm thành phố...")
                .param("starRating", "5")
                .param("email", "contact@grandsaigon.com")
                .param("phone", "02839123456")
                .param("contactName", "Mr. Quan Ly")
                .param("contactPhone", "0909123456")
                .param("isActive", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(1007))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }

    @Test
    void addHotel_WhenInvalidRequest_ShouldReturn400() throws Exception {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "hotel.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes());

        // Do not mock service because validation fails before reaching the service

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/hotels/add")
                .file(imageFile)
                .param("name", "") // Blank name - constraint violation
                .param("location", "Ho Chi Minh City")
                .param("description", "Khách sạn 5 sao trung tâm thành phố...")
                .param("starRating", "6") // Max is 5 - constraint violation
                .param("email", "invalid-email") // Invalid email - constraint violation
                .param("phone", "02839123456")
                .param("contactName", "Mr. Quan Ly")
                .param("contactPhone", "0909123456")
                .param("isActive", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Error"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.starRating").exists())
                .andExpect(jsonPath("$.data.email").exists());

        verify(hotelService, never()).addHotel(any(HotelCreateRequest.class), anyList());
    }

    @Test
    void addHotel_WhenNoImage_ShouldReturn400() throws Exception {
        // Arrange
        when(hotelService.addHotel(any(HotelCreateRequest.class), isNull()))
                .thenThrow(new AppException(ErrorCode.IMAGE_REQUIRED));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/hotels/add")
                .param("name", "Grand Saigon Hotel")
                .param("location", "Ho Chi Minh City")
                .param("description", "Khách sạn 5 sao trung tâm thành phố...")
                .param("starRating", "5")
                .param("email", "contact@grandsaigon.com")
                .param("phone", "02839123456")
                .param("contactName", "Mr. Quan Ly")
                .param("contactPhone", "0909123456")
                .param("isActive", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.IMAGE_REQUIRED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.IMAGE_REQUIRED.getMessage()));
    }

    @Test
    void addHotel_WhenHotelExists_ShouldReturn400() throws Exception {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "hotel.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes());

        when(hotelService.addHotel(any(HotelCreateRequest.class), anyList()))
                .thenThrow(new AppException(ErrorCode.HOTEL_ALREADY_EXISTS));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/hotels/add")
                .file(imageFile)
                .param("name", "Grand Saigon Hotel")
                .param("location", "Ho Chi Minh City")
                .param("description", "Khách sạn 5 sao trung tâm thành phố...")
                .param("starRating", "5")
                .param("email", "contact@grandsaigon.com")
                .param("phone", "02839123456")
                .param("contactName", "Mr. Quan Ly")
                .param("contactPhone", "0909123456")
                .param("isActive", "true")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.HOTEL_ALREADY_EXISTS.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.HOTEL_ALREADY_EXISTS.getMessage()));
    }
}
