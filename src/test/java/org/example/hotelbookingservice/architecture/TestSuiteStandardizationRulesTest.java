package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestSuiteStandardizationRulesTest {

    @Test
    void testsShouldNotBeDisabledOrUseDeprecatedMockBean() throws IOException {
        String disabledAnnotation = "@" + "Disabled";
        String deprecatedMockBean = "@" + "MockBean";

        List<String> violations = javaTestFiles().stream()
                .filter(path -> containsAny(path, disabledAnnotation, deprecatedMockBean))
                .map(Path::toString)
                .toList();

        assertThat(violations)
                .as("Tests should run by default and use @MockitoBean for Spring Boot 3.5 slices.")
                .isEmpty();
    }

    @Test
    void controllerTestsUseSpringMvcSlices() throws IOException {
        String standaloneSetup = "MockMvcBuilders" + ".standaloneSetup";
        String mockitoExtension = "@" + "ExtendWith(MockitoExtension.class)";

        List<String> violations = javaTestFiles().stream()
                .filter(path -> path.toString().contains("/controller/"))
                .filter(path -> containsAny(path, standaloneSetup, mockitoExtension))
                .map(Path::toString)
                .toList();

        assertThat(violations)
                .as("Controller tests should use @WebMvcTest slices with mocked dependencies.")
                .isEmpty();
    }

    @Test
    void integrationTestsUsePostgresqlTestcontainers() throws IOException {
        String testcontainersAnnotation = "@" + "Testcontainers";
        String springBootTestAnnotation = "@" + "SpringBootTest";
        String dataJpaTestAnnotation = "@" + "DataJpaTest";

        List<String> violations = javaTestFiles().stream()
                .filter(path -> path.getFileName().toString().contains("IntegrationTest"))
                .filter(path -> !containsAny(path, testcontainersAnnotation, springBootTestAnnotation, dataJpaTestAnnotation))
                .map(Path::toString)
                .toList();

        assertThat(violations)
                .as("Integration tests should run against the standard Spring context or PostgreSQL Testcontainers.")
                .isEmpty();
    }

    private List<Path> javaTestFiles() throws IOException {
        try (var paths = Files.walk(Path.of("src/test/java"))) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }
    }

    private boolean containsAny(Path path, String... needles) {
        try {
            String source = Files.readString(path);
            for (String needle : needles) {
                if (source.contains(needle)) {
                    return true;
                }
            }
            return false;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
