package org.example.hotelbookingservice.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDenialHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // Create ApiResponse with error message
        ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                .status(HttpStatus.FORBIDDEN.value()) // 403
                .message("You do not have permission to access this resource") // Custom message cho rõ ràng hơn
                .build();

        // Set up JSON return header
        response.setContentType("application/json");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Write ApiResponse object to JSON string in response body
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
