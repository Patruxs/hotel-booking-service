package org.example.hotelbookingservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleUnwantedException_returnsGenericMessageWithoutLeakingDetails() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException exception = new RuntimeException("select * from accounts where password_hash = 'secret'");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");

        ProblemDetail problem = handler.handleUnwantedException(exception, request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
        assertThat(problem.getDetail()).isEqualTo("Internal Server Error");
        assertThat(problem.getProperties().get("errorCode")).isEqualTo("INTERNAL_ERROR");
        assertThat(problem.getInstance().toString()).isEqualTo("/api/v1/test");
    }
}
