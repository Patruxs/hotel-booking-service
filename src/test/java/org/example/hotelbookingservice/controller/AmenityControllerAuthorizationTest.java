package org.example.hotelbookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.hotelbookingservice.dto.request.amenity.AmenityRequest;
import org.example.hotelbookingservice.dto.response.AmenityResponse;
import org.example.hotelbookingservice.services.IAmenityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AmenityController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(AmenityControllerAuthorizationTest.MethodSecurityTestConfig.class)
class AmenityControllerAuthorizationTest {

    @TestConfiguration
    @EnableMethodSecurity(proxyTargetClass = true)
    static class MethodSecurityTestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAmenityService amenityService;

    @MockitoBean
    private org.example.hotelbookingservice.security.JwtUtils jwtUtils;

    @MockitoBean
    private org.example.hotelbookingservice.security.CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private org.example.hotelbookingservice.exception.CustomAccessDenialHandler customAccessDenialHandler;

    @MockitoBean
    private org.example.hotelbookingservice.exception.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser(authorities = "OWNER")
    void createAmenity_whenOwner_shouldReturnCreatedAmenity() throws Exception {
        AmenityRequest request = amenityRequest("Owner Pool");
        when(amenityService.createAmenity(any(AmenityRequest.class)))
                .thenReturn(amenityResponse(10, "Owner Pool"));

        mockMvc.perform(post("/api/v1/amenities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @WithMockUser(authorities = "OWNER")
    void updateAmenity_whenOwner_shouldReturnUpdatedAmenity() throws Exception {
        AmenityRequest request = amenityRequest("Updated Pool");
        when(amenityService.updateAmenity(eq(10), any(AmenityRequest.class)))
                .thenReturn(amenityResponse(10, "Updated Pool"));

        mockMvc.perform(put("/api/v1/amenities/update/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.name").value("Updated Pool"));
    }

    @Test
    @WithMockUser(authorities = "OWNER")
    void deleteAmenity_whenOwner_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/amenities/delete/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(amenityService).deleteAmenity(10);
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void createAmenity_whenCustomer_shouldRemainForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/amenities/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amenityRequest("Customer Pool"))))
                .andExpect(status().isForbidden());
    }

    private AmenityRequest amenityRequest(String name) {
        AmenityRequest request = new AmenityRequest();
        request.setName(name);
        request.setType("HOTEL");
        return request;
    }

    private AmenityResponse amenityResponse(Integer id, String name) {
        return AmenityResponse.builder()
                .id(id)
                .name(name)
                .type("HOTEL")
                .build();
    }
}
