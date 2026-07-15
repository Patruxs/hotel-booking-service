package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityQualityRulesTest {

    @Test
    void refreshTokensUseSha256HashesInsteadOfPasswordEncoder() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/org/example/hotelbookingservice/services/impl/AuthAccountServiceImpl.java"));

        assertThat(source).contains("MessageDigest.getInstance(\"SHA-256\")");
        assertThat(source).contains("refresh_token_hash");
        assertThat(source).doesNotContain("BCryptPasswordEncoder");
        assertThat(source).doesNotContain("passwordEncoder.encode(refreshToken)");
        assertThat(source).doesNotContain("passwordEncoder.matches(refreshToken");
    }

    @Test
    void securityFilterKeepsMyHotelsAuthenticatedAndUsesCorrectName() throws IOException {
        assertThat(Path.of("src/main/java/org/example/hotelbookingservice/security/SecurityFilter.java")).exists();
        assertThat(Path.of("src/main/java/org/example/hotelbookingservice/security/SecurtyFilter.java")).doesNotExist();

        String source = Files.readString(Path.of(
                "src/main/java/org/example/hotelbookingservice/security/SecurityFilter.java"));

        assertThat(source).doesNotContain("/api/v1/hotels/my-hotels");
        assertThat(source).contains(".anyRequest().authenticated()");
    }
}
