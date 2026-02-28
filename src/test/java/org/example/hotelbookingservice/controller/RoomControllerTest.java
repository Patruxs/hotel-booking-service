package org.example.hotelbookingservice.controller;

import org.example.hotelbookingservice.dto.response.RoomResponse;
import org.example.hotelbookingservice.exception.GlobalExceptionHandler;
import org.example.hotelbookingservice.services.IRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.example.hotelbookingservice.dto.request.room.RoomCreateRequest;
import org.example.hotelbookingservice.enums.RoomType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IRoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addRoom_WhenValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "room.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        RoomResponse mockResponse = new RoomResponse();
        mockResponse.setId(1);
        mockResponse.setName("Deluxe Ocean View");
        mockResponse.setType(RoomType.SINGLE);
        mockResponse.setPrice(new BigDecimal("500000"));

        when(roomService.addRoom(any(RoomCreateRequest.class), any(MockMultipartFile.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/rooms/add")
                        .file(image)
                        .param("hotelId", "1")
                        .param("type", "SINGLE")
                        .param("price", "500000")
                        .param("capacity", "2")
                        .param("description", "Ocean view room")
                        .param("name", "Deluxe Ocean View")
                        .param("amount", "5")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Room successfully added"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Deluxe Ocean View"))
                .andExpect(jsonPath("$.data.type").value("SINGLE"))
                .andExpect(jsonPath("$.data.price").value(500000));
    }

    @Test
    void addRoom_WhenInvalidRequest_ShouldReturn400() throws Exception {
        // Arrange
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "room.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        // Act & Assert
        // Missing required hotelId, type, price, etc.
        mockMvc.perform(multipart("/api/v1/rooms/add")
                        .file(image)
                        // Intentionally invalid capacity and price to trigger validation
                        .param("price", "-100")
                        .param("capacity", "0")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Error"));
    }

    @Test
    void addRoom_WhenCustomer_ShouldReturn403() throws Exception {
        // Arrange
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "room.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        doThrow(new AccessDeniedException("Access Denied"))
                .when(roomService).addRoom(any(RoomCreateRequest.class), any(MockMultipartFile.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/rooms/add")
                        .file(image)
                        .param("hotelId", "1")
                        .param("type", "SINGLE")
                        .param("price", "500000")
                        .param("capacity", "2")
                        .param("description", "Ocean view room")
                        .param("name", "Deluxe Ocean View")
                        .param("amount", "5")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(1007))
                .andExpect(jsonPath("$.message").value("You do not have permission"));
    }
}
