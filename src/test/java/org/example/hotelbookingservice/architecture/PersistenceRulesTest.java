package org.example.hotelbookingservice.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceRulesTest {

    private static final Pattern QUERY_ANNOTATION = Pattern.compile("@Query\\s*\\((?s:.*?)\\)");

    @Test
    void newCustomRepositoryQueriesUseNativeSqlInsteadOfJpql() throws IOException {
        Path sourceRoot = Path.of("src/main/java");
        Set<String> quarantinedLegacyFiles = Set.of(
                "src/main/java/org/example/hotelbookingservice/repository/BookingRepository.java",
                "src/main/java/org/example/hotelbookingservice/repository/HotelRepository.java",
                "src/main/java/org/example/hotelbookingservice/repository/HotelamenityRepository.java",
                "src/main/java/org/example/hotelbookingservice/repository/ReviewRepository.java",
                "src/main/java/org/example/hotelbookingservice/repository/RoomRepository.java",
                "src/main/java/org/example/hotelbookingservice/repository/RoomamenityRepository.java",
                "src/main/java/org/example/hotelbookingservice/repository/UserRepository.java"
        );

        List<String> jpqlQueries;
        try (var paths = Files.walk(sourceRoot)) {
            jpqlQueries = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> findNonNativeQueryAnnotations(path).stream())
                    .filter(query -> quarantinedLegacyFiles.stream().noneMatch(query::startsWith))
                    .toList();
        }

        assertThat(jpqlQueries)
                .as("New custom queries must be raw SQL/native queries. Prefer NamedParameterJdbcTemplate for new custom persistence.")
                .isEmpty();
    }

    @Test
    void persistenceRuleDocumentationExists() {
        assertThat(Path.of("docs/backend/persistence-rules.md")).exists();
        assertThat(Path.of("docs/backend/legacy-jpql-debt.md")).exists();
    }

    private List<String> findNonNativeQueryAnnotations(Path path) {
        try {
            String content = Files.readString(path);
            Matcher matcher = QUERY_ANNOTATION.matcher(content);
            return matcher.results()
                    .map(result -> result.group())
                    .filter(annotation -> !annotation.contains("nativeQuery = true"))
                    .map(annotation -> path + " " + annotation.lines().findFirst().orElse("@Query"))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + path, exception);
        }
    }
}
