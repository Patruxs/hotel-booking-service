package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiAuthSchemaTest {

    @Test
    void authLoginSchemaDocumentsAccessToken() throws IOException {
        String authApi = Files.readString(Path.of("src/main/java/org/example/hotelbookingservice/api/AuthApi.java"));
        String authResponses = Files.readString(Path.of("src/main/java/org/example/hotelbookingservice/dto/response/auth/AuthResponses.java"));

        assertThat(authApi)
                .contains("TokenApiResponse")
                .contains("\"accessToken\"");
        assertThat(authResponses)
                .contains("record TokenResponse")
                .contains("String accessToken")
                .contains("record TokenApiResponse");
    }

    @Test
    void legacyLoginResponseDoesNotAdvertiseTokenProperty() throws IOException {
        String loginResponse = Files.readString(Path.of("src/main/java/org/example/hotelbookingservice/dto/response/LoginResponse.java"));
        String userService = Files.readString(Path.of("src/main/java/org/example/hotelbookingservice/services/impl/UserServiceImpl.java"));

        assertThat(loginResponse)
                .contains("private String accessToken;")
                .doesNotContain("private String token;");
        assertThat(userService)
                .contains(".accessToken(token)")
                .doesNotContain(".token(token)");
    }
}
