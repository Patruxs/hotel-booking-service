package org.example.hotelbookingservice.exception;

import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void handleJsonParseException_prodProfile_hidesExceptionMessage() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true); // prod profile
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Sensitive parse error details");

        // When
        ResponseEntity<ApiResponse<String>> response = globalExceptionHandler.handleJsonParseException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ErrorCode.JSON_PARSE_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid JSON request format");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    void handleJsonParseException_nonProdProfile_showsExceptionMessage() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false); // not prod profile
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Sensitive parse error details");

        // When
        ResponseEntity<ApiResponse<String>> response = globalExceptionHandler.handleJsonParseException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ErrorCode.JSON_PARSE_ERROR.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid JSON request format");
        assertThat(response.getBody().getData()).isEqualTo("Sensitive parse error details");
    }

    @Test
    void handleUnwantedException_prodProfile_hidesExceptionMessage() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true); // prod profile
        Exception ex = new RuntimeException("Database connection failed");

        // When
        ResponseEntity<ApiResponse<String>> response = globalExceptionHandler.handleUnwantedException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("Internal Server Error");
    }

    @Test
    void handleUnwantedException_nonProdProfile_showsExceptionMessage() {
        // Given
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false); // not prod profile
        Exception ex = new RuntimeException("Database connection failed");

        // When
        ResponseEntity<ApiResponse<String>> response = globalExceptionHandler.handleUnwantedException(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        assertThat(response.getBody().getMessage()).isEqualTo("Internal Server Error: Database connection failed");
    }
}
