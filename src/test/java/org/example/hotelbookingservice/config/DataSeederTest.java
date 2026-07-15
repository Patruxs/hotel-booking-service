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

class DataSeederTest {

    @Test
    void localProfile_registersOnlyTheComprehensiveSeeder() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("local");
            context.register(DataSeeder.class, DemoBootstrapData.class);

            assertThat(context.containsBeanDefinition("dataSeeder")).isTrue();
            assertThat(context.containsBeanDefinition("demoBootstrapData")).isFalse();
        }
    }

    @Test
    void run_seedsDataForTheCoreDemoWorkflows() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenAnswer(invocation -> ((MapSqlParameterSource) invocation.getArgument(1)).getValue("id"));
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));

        new DataSeeder(jdbcTemplate, passwordEncoder).run();

          ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
          ArgumentCaptor<MapSqlParameterSource> parameters = ArgumentCaptor.forClass(MapSqlParameterSource.class);
          verify(jdbcTemplate, org.mockito.Mockito.atLeast(100)).update(sql.capture(), parameters.capture());
        List<String> statements = sql.getAllValues();
          assertThat(statements).anyMatch(statement -> statement.contains("insert into hotels"));
          assertThat(statements).anyMatch(statement -> statement.contains("owner_id = excluded.owner_id"));
        assertThat(statements).anyMatch(statement -> statement.contains("delete from account_roles"));
        assertThat(statements).anyMatch(statement -> statement.contains("insert into room_types"));
        assertThat(statements).anyMatch(statement -> statement.contains("insert into bookings"));
        assertThat(statements).anyMatch(statement -> statement.contains("insert into payments"));
        assertThat(statements).anyMatch(statement -> statement.contains("insert into promotions"));
          assertThat(statements).anyMatch(statement -> statement.contains("insert into news"));
          assertThat(statements).anyMatch(statement -> statement.contains("insert into hotel_images"));
          assertThat(statements).anyMatch(statement -> statement.contains("insert into room_type_images"));
          assertThat(statements).anyMatch(statement -> statement.contains("insert into news_images"));
          assertThat(statements).anyMatch(statement -> statement.contains("insert into contact_messages"));
        assertThat(parameters.getAllValues())
                .extracting(source -> source.hasValue("url") ? source.getValue("url") : null)
                .contains("https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699214/small_andrew-neel-B4rEJ09-Puo-unsplash_ilcr8p.jpg",
                          "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_the-anam-_twiIcIsp2s-unsplash_wlcji8.jpg",
                          "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699214/small_cory-bjork-D1yT791Nf9A-unsplash_otawcw.jpg",
                          "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699215/small_mark-champs-Id2IIl1jOB0-unsplash_e1phl6.jpg",
                          "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699217/small_sasha-kaunas-67-sOi7mVIk-unsplash_1_wgsg94.jpg",
                          "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783699218/small_vojtech-bruzek-Yrxr3bsPdS0-unsplash_ciknsd.jpg");
        assertThat(statements).anyMatch(statement -> statement.contains("'CLOUDINARY'"));
        assertThat(statements).noneMatch(statement -> statement.contains("/images/demo/"));
        assertThat(parameters.getAllValues())
                .filteredOn(source -> source.hasValue("hotelId"))
                .extracting(source -> source.getValue("hotelId"))
                .contains(UUID.fromString("6f3924c0-61c6-4c86-8bdc-b1f468e04468"));
        assertThat(parameters.getAllValues())
                .filteredOn(source -> source.hasValue("hotelId") && source.hasValue("imageId") && source.hasValue("sortOrder")
                        && UUID.fromString("40000000-0000-4000-8000-000000000102").equals(source.getValue("hotelId")))
                .hasSize(4);
    }

    @Test
    void run_whenAdminEmailAlreadyExists_usesTheExistingAccountIdForRelatedFixtures() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UUID legacyAdminId = UUID.fromString("30000000-0000-4000-8000-000000000001");
        when(jdbcTemplate.update(anyString(), any(MapSqlParameterSource.class))).thenReturn(1);
        when(jdbcTemplate.queryForObject(anyString(), any(MapSqlParameterSource.class), any(Class.class)))
                .thenAnswer(invocation -> {
                    MapSqlParameterSource parameters = invocation.getArgument(1);
                    return "admin@gmail.com".equals(parameters.getValue("email"))
                            ? legacyAdminId
                            : parameters.getValue("id");
                });
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        new DataSeeder(jdbcTemplate, passwordEncoder).run();

        ArgumentCaptor<String> accountSql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MapSqlParameterSource> accountParameters = ArgumentCaptor.forClass(MapSqlParameterSource.class);
          verify(jdbcTemplate, org.mockito.Mockito.times(6))
                .queryForObject(accountSql.capture(), accountParameters.capture(), org.mockito.ArgumentMatchers.eq(UUID.class));
        assertThat(accountSql.getAllValues()).allMatch(sql -> sql.contains("on conflict (email)") && sql.contains("returning id"));

        ArgumentCaptor<MapSqlParameterSource> updateParameters = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(jdbcTemplate, org.mockito.Mockito.atLeast(100)).update(anyString(), updateParameters.capture());
          assertThat(updateParameters.getAllValues())
                  .filteredOn(parameters -> parameters.hasValue("roleName") && "ADMIN".equals(parameters.getValue("roleName")))
                  .extracting(parameters -> parameters.getValue("accountId"))
                  .containsExactly(legacyAdminId);
          assertThat(updateParameters.getAllValues())
                  .filteredOn(parameters -> parameters.hasValue("roleName") && "OWNER".equals(parameters.getValue("roleName")))
                  .extracting(parameters -> parameters.getValue("accountId"))
                  .containsExactly(UUID.fromString("40000000-0000-4000-8000-000000000002"));
          assertThat(updateParameters.getAllValues())
                  .filteredOn(parameters -> parameters.hasValue("ownerId") && parameters.hasValue("rating"))
                  .extracting(parameters -> parameters.getValue("ownerId"))
                  .containsOnly(UUID.fromString("40000000-0000-4000-8000-000000000002"));
        assertThat(updateParameters.getAllValues())
                .filteredOn(parameters -> parameters.hasValue("authorId"))
                .extracting(parameters -> parameters.getValue("authorId"))
                .containsOnly(legacyAdminId);
    }
}
