package org.example.hotelbookingservice.repository;

import org.example.hotelbookingservice.entity.RolePermissionId;
import org.example.hotelbookingservice.entity.UserRoleId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IdentityJpaMappingTest {
    private static final UUID ACCOUNT_ID = UUID.fromString("50000000-0000-4000-8000-000000000001");
    private static final UUID ROLE_ID = UUID.fromString("00000000-0000-4000-8000-000000000006");
    private static final UUID PERMISSION_ID = UUID.fromString("00000000-0000-4001-8000-000000000008");
    private static final UUID ACTION_ID = UUID.fromString("00000000-0000-4002-8000-000000000007");
    private static final UUID POLICY_ID = UUID.fromString("10000000-0000-4003-8000-000000000003");

    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PermissionRepository permissionRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired RolePermissionRepository rolePermissionRepository;
    @Autowired AuthSessionRepository authSessionRepository;
    @Autowired EmailVerifyTokenRepository emailVerifyTokenRepository;
    @Autowired PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired ApiActionRepository apiActionRepository;
    @Autowired ApiActionPolicyRepository apiActionPolicyRepository;

    @Test
    void identityEntitiesLoadAgainstFlywaySchema() {
        insertAccount();
        insertSessionAndTokens();
        insertAccountRole();

        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(ACCOUNT_ID);
        userRoleId.setRoleId(ROLE_ID);
        RolePermissionId rolePermissionId = new RolePermissionId();
        rolePermissionId.setRoleId(ROLE_ID);
        rolePermissionId.setPermissionId(PERMISSION_ID);

        var user = userRepository.findById(ACCOUNT_ID).orElseThrow();
        var role = roleRepository.findById(ROLE_ID).orElseThrow();
        var permission = permissionRepository.findById(PERMISSION_ID).orElseThrow();
        var userRole = userRoleRepository.findById(userRoleId).orElseThrow();
        var rolePermission = rolePermissionRepository.findById(rolePermissionId).orElseThrow();
        var session = authSessionRepository.findByJti("identity-mapping-jti").orElseThrow();
        var verifyToken = emailVerifyTokenRepository.findByTokenHash("identity-verify-hash").orElseThrow();
        var resetToken = passwordResetTokenRepository.findByTokenHash("identity-reset-hash").orElseThrow();
        var action = apiActionRepository.findById(ACTION_ID).orElseThrow();
        var policy = apiActionPolicyRepository.findById(POLICY_ID).orElseThrow();

        assertThat(user.getEmailVerified()).isTrue();
        assertThat(user.getProvider()).isEqualTo("LOCAL");
        assertThat(role.getName()).isEqualTo("CUSTOMER");
        assertThat(permission.getKey()).isEqualTo("bookings.create");
        assertThat(userRole.getUser().getEmail()).isEqualTo("identity-mapping@example.com");
        assertThat(rolePermission.getPermission().getKey()).isEqualTo("bookings.create");
        assertThat(session.getRefreshHash()).isEqualTo("identity-refresh-hash");
        assertThat(session.getIp()).isEqualTo("127.0.0.1");
        assertThat(verifyToken.getUser().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(resetToken.getUserId()).isEqualTo(ACCOUNT_ID);
        assertThat(action.getKey()).isEqualTo("bookings.create");
        assertThat(policy.getPermission().getKey()).isEqualTo("bookings.create");
    }

    @Test
    void seededAuthorizationCatalogRowsRemainQueryableThroughAlignedRepositories() {
        assertThat(roleRepository.findByName("ADMIN")).isPresent();
        assertThat(permissionRepository.findByKey("security.manage")).isPresent();
        assertThat(apiActionRepository.findByKey("actions.list")).isPresent();
        assertThat(apiActionPolicyRepository.count()).isGreaterThanOrEqualTo(23);
        assertThat(rolePermissionRepository.count()).isGreaterThanOrEqualTo(8);
    }

    private void insertAccount() {
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified, auth_provider)
                values (?, 'identity-mapping@example.com', 'hash', 'Identity', 'Mapping', true, 'LOCAL')
                on conflict (id) do nothing
                """, ACCOUNT_ID);
    }

    private void insertSessionAndTokens() {
        Instant expiresAt = Instant.now().plusSeconds(1800);
        jdbc.update("""
                insert into auth_sessions (id, account_id, jti, refresh_token_hash, provider, ip_address, user_agent, expires_at)
                values (?, ?, 'identity-mapping-jti', 'identity-refresh-hash', 'LOCAL', '127.0.0.1', 'JUnit', ?)
                on conflict (jti) do nothing
        """, UUID.fromString("50000000-0000-4000-8000-000000000002"), ACCOUNT_ID, Timestamp.from(expiresAt));
        jdbc.update("""
                insert into email_verification_tokens (id, account_id, token_hash, expires_at)
                values (?, ?, 'identity-verify-hash', ?)
                on conflict (token_hash) do nothing
        """, UUID.fromString("50000000-0000-4000-8000-000000000003"), ACCOUNT_ID, Timestamp.from(expiresAt));
        jdbc.update("""
                insert into password_reset_tokens (id, account_id, token_hash, expires_at)
                values (?, ?, 'identity-reset-hash', ?)
                on conflict (token_hash) do nothing
        """, UUID.fromString("50000000-0000-4000-8000-000000000004"), ACCOUNT_ID, Timestamp.from(expiresAt));
    }

    private void insertAccountRole() {
        jdbc.update("""
                insert into account_roles (account_id, role_id)
                values (?, ?)
                on conflict do nothing
                """, ACCOUNT_ID, ROLE_ID);
    }
}
