package org.example.hotelbookingservice.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthFilterTest {
    private final Logger authLogger = (Logger) LoggerFactory.getLogger(AuthFilter.class);

    @AfterEach
    void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void malformedJwtContinuesAsUnauthenticatedWithoutErrorLog() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(jwtUtils.getUsernameFromToken("invalid-token"))
                .thenThrow(new io.jsonwebtoken.JwtException("Invalid compact JWT string"));

        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        authLogger.addAppender(appender);
        try {
            AuthFilter filter = new AuthFilter(jwtUtils, userDetailsService);
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(appender.list)
                    .filteredOn(event -> event.getLevel() == Level.ERROR)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .noneMatch(message -> message.contains("Cannot set user authentication"));
        } finally {
            authLogger.detachAppender(appender);
            appender.stop();
        }
    }
}
