package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.config.UploadProperties;
import org.example.hotelbookingservice.dto.request.content.CommissionPackageRequest;
import org.example.hotelbookingservice.entity.CommissionPackage;
import org.example.hotelbookingservice.repository.CommissionPackageRepository;
import org.example.hotelbookingservice.repository.HotelCommissionPackageRepository;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.example.hotelbookingservice.services.IFileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentCommissionPackageServiceTest {
    @Mock NamedParameterJdbcTemplate jdbcTemplate;
    @Mock IFileStorageService fileStorageService;
    @Mock UploadProperties uploadProperties;
    @Mock CommissionPackageRepository commissionPackageRepository;
    @Mock HotelCommissionPackageRepository hotelCommissionPackageRepository;

    @Test
    void listCommissionPackages_mapsRepositoryEntitiesToExistingResponseShape() {
        ContentServiceImpl service = service();
        CommissionPackage commissionPackage = commissionPackage();
        when(jdbcTemplate.queryForObject(anyString(), any(SqlParameterSource.class), eq(Boolean.class))).thenReturn(true);
        when(commissionPackageRepository.findAllByOrderByActiveDescCodeAsc()).thenReturn(List.of(commissionPackage));

        var response = service.listCommissionPackages(adminAuthentication());

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(commissionPackage.getId());
        assertThat(response.getFirst().code()).isEqualTo("FOCUSED");
        assertThat(response.getFirst().commissionRate()).isEqualByComparingTo("0.1500");
        assertThat(response.getFirst().isActive()).isTrue();
    }

    @Test
    void createCommissionPackage_savesThroughRepositoryAndPreservesResponseShape() {
        ContentServiceImpl service = service();
        when(jdbcTemplate.queryForObject(anyString(), any(SqlParameterSource.class), eq(Boolean.class))).thenReturn(true);
        when(commissionPackageRepository.saveAndFlush(any(CommissionPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<CommissionPackage> saved = ArgumentCaptor.forClass(CommissionPackage.class);

        var response = service.createCommissionPackage(
                new CommissionPackageRequest("focused", "Focused", "Repository path", new BigDecimal("0.1500"), true),
                adminAuthentication()
        );

        verify(commissionPackageRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getCode()).isEqualTo("FOCUSED");
        assertThat(response.code()).isEqualTo("FOCUSED");
        assertThat(response.name()).isEqualTo("Focused");
        assertThat(response.commissionRate()).isEqualByComparingTo("0.1500");
    }

    private ContentServiceImpl service() {
        return new ContentServiceImpl(
                jdbcTemplate,
                fileStorageService,
                uploadProperties,
                commissionPackageRepository,
                hotelCommissionPackageRepository
        );
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AccountAuthUser user = AccountAuthUser.builder()
                .accountId(UUID.fromString("72000000-0000-4000-8000-000000000001"))
                .email("admin@example.com")
                .passwordHash("hash")
                .emailVerified(true)
                .authorities(List.of())
                .build();
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private CommissionPackage commissionPackage() {
        CommissionPackage commissionPackage = new CommissionPackage();
        commissionPackage.setId(UUID.fromString("72000000-0000-4000-8000-000000000002"));
        commissionPackage.setCode("FOCUSED");
        commissionPackage.setName("Focused");
        commissionPackage.setDescription("Repository path");
        commissionPackage.setCommissionRate(new BigDecimal("0.1500"));
        commissionPackage.setActive(true);
        commissionPackage.setCreatedAt(Instant.parse("2026-07-09T00:00:00Z"));
        commissionPackage.setUpdatedAt(Instant.parse("2026-07-09T00:00:00Z"));
        return commissionPackage;
    }
}
