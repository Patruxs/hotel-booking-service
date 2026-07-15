package org.example.hotelbookingservice.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DemoBootstrapDataTest {

    @Test
    void legacyBootstrapProfile_excludesTheComprehensiveSeeder() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("local", "legacy-demo-bootstrap");
            context.register(DataSeeder.class, DemoBootstrapData.class);

            assertThat(context.containsBeanDefinition("dataSeeder")).isFalse();
            assertThat(context.containsBeanDefinition("demoBootstrapData")).isTrue();
        }
    }

    @Test
    void run_seedsDocumentedAdminAndCustomerCredentials() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        when(passwordEncoder.encode("admin123")).thenReturn("encoded-admin");
        when(passwordEncoder.encode("customer123")).thenReturn("encoded-customer");
          when(passwordEncoder.encode("password123")).thenReturn("encoded-operator");
          when(passwordEncoder.encode("owner123")).thenReturn("encoded-owner");

        new DemoBootstrapData(jdbcTemplate, passwordEncoder).run();

        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate, org.mockito.Mockito.atLeast(3)).update(anyString(), params.capture());

        List<MapSqlParameterSource> accountParams = params.getAllValues().stream()
                .filter(source -> source.hasValue("email"))
                .toList();
        assertThat(accountParams)
                .extracting(source -> source.getValue("email"))
                  .contains("admin@gmail.com", "customer@gmail.com", "owner@grand.test");

        MapSqlParameterSource admin = accountParams.stream()
                .filter(source -> "admin@gmail.com".equals(source.getValue("email")))
                .findFirst()
                .orElseThrow();
        assertThat(admin.getValue("password")).isEqualTo("encoded-admin");
        assertThat(admin.getValue("firstName")).isEqualTo("Admin");
        assertThat(admin.getValue("lastName")).isEqualTo("User");

        MapSqlParameterSource customer = accountParams.stream()
                .filter(source -> "customer@gmail.com".equals(source.getValue("email")))
                .findFirst()
                .orElseThrow();
          assertThat(customer.getValue("password")).isEqualTo("encoded-customer");

          MapSqlParameterSource owner = accountParams.stream()
                  .filter(source -> "owner@grand.test".equals(source.getValue("email")))
                  .findFirst()
                  .orElseThrow();
          assertThat(owner.getValue("id")).isEqualTo(UUID.fromString("30000000-0000-4000-8000-000000000004"));
          assertThat(owner.getValue("password")).isEqualTo("encoded-owner");
          assertThat(params.getAllValues())
                  .filteredOn(source -> source.hasValue("roleName") && "OWNER".equals(source.getValue("roleName")))
                  .extracting(source -> source.getValue("accountId"))
                  .containsExactly(owner.getValue("id"));
          assertThat(params.getAllValues())
                  .filteredOn(source -> source.hasValue("ownerId")
                          && UUID.fromString("30000000-0000-4000-8000-000000000101").equals(source.getValue("id")))
                  .extracting(source -> source.getValue("ownerId"))
                  .containsOnly(owner.getValue("id"));

          ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
          verify(jdbcTemplate, org.mockito.Mockito.atLeast(1)).update(sql.capture(), any(MapSqlParameterSource.class));
          assertThat(sql.getAllValues())
                  .anyMatch(statement -> statement.contains("owner_id = excluded.owner_id"));
      }
}
