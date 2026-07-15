package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.request.content.BannerMutationRequest;
import org.example.hotelbookingservice.entity.Banner;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.example.hotelbookingservice.services.impl.ContentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BannerCreationJpaTest {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private BannerImageRepository bannerImageRepository;

    @Test
    void createBannerPersistsWithRepositoryGeneratedIdentifier() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), any(SqlParameterSource.class), eq(Boolean.class))).thenReturn(true);

        ContentServiceImpl contentService = new ContentServiceImpl(
                jdbcTemplate,
                mock(IFileStorageService.class),
                new UploadProperties("LOCAL", 12),
                null,
                null,
                null,
                bannerRepository,
                null,
                bannerImageRepository,
                null,
                null
        );

        var response = contentService.createBanner(
                new BannerMutationRequest(
                        "Repository banner",
                        "Subtitle",
                        "/hotels",
                        "URL",
                        910001,
                        true,
                        null,
                        null,
                        null,
                        List.of("https://example.com/repository-banner.png")
                ),
                adminAuthentication()
        );

        assertThat(response.id()).isNotNull();
        Banner persisted = bannerRepository.findById(response.id()).orElseThrow();
        assertThat(persisted.getTitle()).isEqualTo("Repository banner");
        assertThat(bannerImageRepository.findByBanner_IdOrderBySortOrderAsc(response.id()))
                .singleElement()
                .extracting(image -> image.getUrl())
                  .isEqualTo("https://example.com/repository-banner.png");
    }

    @Test
    void createBannerRejectsStartDateBeforeToday() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), any(SqlParameterSource.class), eq(Boolean.class))).thenReturn(true);

        ContentServiceImpl contentService = new ContentServiceImpl(
                jdbcTemplate,
                mock(IFileStorageService.class),
                new UploadProperties("LOCAL", 12),
                null,
                null,
                null,
                bannerRepository,
                null,
                bannerImageRepository,
                null,
                null
        );

        assertThatThrownBy(() -> contentService.createBanner(
                new BannerMutationRequest(
                        "Past banner",
                        null,
                        "/hotels",
                        "URL",
                        910002,
                        true,
                        Instant.parse("2000-01-01T00:00:00Z"),
                        null,
                        null,
                        List.of("https://example.com/past-banner.png")
                ),
                adminAuthentication()
        ))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Banner start date cannot be before today");
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AccountAuthUser user = AccountAuthUser.builder()
                .accountId(java.util.UUID.fromString("76000000-0000-4000-8000-000000000001"))
                .email("admin@example.com")
                .passwordHash("hash")
                .emailVerified(true)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
