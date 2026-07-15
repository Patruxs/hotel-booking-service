package org.example.hotelbookingservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIdentityFlowIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void authFlowsPreservePublicResponseShapes() throws Exception {
        String email = "identity-auth-" + UUID.randomUUID() + "@example.com";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "fullName", "Identity Auth",
                                "email", email,
                                "phone", "0900000001",
                                "password", "Password1!",
                                "dob", LocalDate.of(1995, 5, 20).toString()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").isNotEmpty())
                .andExpect(jsonPath("$.data.emailVerified").value(false));

        UUID accountId = jdbc.queryForObject("select id from accounts where email = ?", UUID.class, email);
        String verifyToken = "verify-token-" + UUID.randomUUID();
        jdbc.update("""
                update email_verification_tokens
                set token_hash = ?, expires_at = now() + interval '30 minutes', used_at = null
                where account_id = ?
                """, sha256(verifyToken), accountId);

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", verifyToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Email verified successfully"));

        mockMvc.perform(post("/api/v1/auth/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(cookie().exists("refresh_token"))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");
        assertThat(refreshCookie).isNotNull();

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists("refresh_token"));

        mockMvc.perform(post("/api/v1/auth/logout").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        String resetToken = "reset-token-" + UUID.randomUUID();
        jdbc.update("""
                update password_reset_tokens
                set token_hash = ?, expires_at = now() + interval '30 minutes', used_at = null
                where account_id = ?
                """, sha256(resetToken), accountId);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "token", resetToken,
                                "newPassword", "NewPassword1!",
                                "confirmPassword", "NewPassword1!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "NewPassword1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
