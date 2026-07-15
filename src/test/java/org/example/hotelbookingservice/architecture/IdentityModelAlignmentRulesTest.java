package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IdentityModelAlignmentRulesTest {
    private static final List<Path> IDENTITY_ENTITIES = List.of(
            Path.of("src/main/java/org/example/hotelbookingservice/entity/User.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/Role.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/Permission.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/UserRole.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/RolePermission.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/AuthSession.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/EmailVerifyToken.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/PasswordResetToken.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/ApiAction.java"),
            Path.of("src/main/java/org/example/hotelbookingservice/entity/ApiActionPolicy.java")
    );

    @Test
    void identityEntitiesDoNotExposeFakeIntegerIdAccessors() throws IOException {
        List<String> violations = IDENTITY_ENTITIES.stream()
                .flatMap(path -> violations(path).stream())
                .toList();

        assertThat(violations)
                .as("UUID-backed identity entities must not reintroduce fake Integer ID compatibility accessors")
                .isEmpty();
    }

    @Test
    void identityRepositoriesDoNotUseIntegerIds() throws IOException {
        Path repositoryRoot = Path.of("src/main/java/org/example/hotelbookingservice/repository");

        List<String> violations;
        try (var paths = Files.walk(repositoryRoot)) {
            violations = paths
                    .filter(path -> path.toString().endsWith("Repository.java"))
                    .filter(path -> isIdentityRepository(path.getFileName().toString()))
                    .filter(path -> content(path).contains("JpaRepository<")
                            && content(path).matches("(?s).*JpaRepository<[^>]+,\\s*Integer>.*"))
                    .map(Path::toString)
                    .toList();
        }

        assertThat(violations)
                .as("Identity repositories must use UUID or embedded ID types, never Integer")
                .isEmpty();
    }

    private List<String> violations(Path path) {
        String content = content(path);
        return content.lines()
                .filter(line -> line.contains("Integer getId()") || line.contains("setId(Integer"))
                .map(line -> path + ": " + line.trim())
                .toList();
    }

    private boolean isIdentityRepository(String fileName) {
        return List.of(
                "UserRepository.java",
                "RoleRepository.java",
                "PermissionRepository.java",
                "UserRoleRepository.java",
                "RolePermissionRepository.java",
                "AuthSessionRepository.java",
                "EmailVerifyTokenRepository.java",
                "PasswordResetTokenRepository.java",
                "ApiActionRepository.java",
                "ApiActionPolicyRepository.java"
        ).contains(fileName);
    }

    private String content(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
