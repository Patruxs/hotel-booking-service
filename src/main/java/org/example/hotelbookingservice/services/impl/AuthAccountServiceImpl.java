package org.example.hotelbookingservice.services.impl;

import org.example.hotelbookingservice.services.IAuthAccountService;
import org.example.hotelbookingservice.services.IAuthAccountService.TokenIssue;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hotelbookingservice.dto.request.auth.*;
import org.example.hotelbookingservice.dto.request.user.AdminUserUpdateRequest;
import org.example.hotelbookingservice.dto.request.user.ChangePasswordRequest;
import org.example.hotelbookingservice.dto.request.user.UserUpdateRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.*;
import org.example.hotelbookingservice.security.AccountAuthUser;
import org.example.hotelbookingservice.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthAccountServiceImpl implements IAuthAccountService {
    private static final UUID CUSTOMER_ROLE_ID = UUID.fromString("00000000-0000-4000-8000-000000000006");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        validatePassword(request.getPassword(), request.getPassword());

        UUID accountId = UUID.randomUUID();
        String[] nameParts = splitName(request.getFullName());
        jdbcTemplate.update("""
                        insert into accounts (id, email, password_hash, first_name, last_name, phone, date_of_birth, email_verified, auth_provider)
                        values (:id, :email, :passwordHash, :firstName, :lastName, :phone, :dateOfBirth, false, 'LOCAL')
                        """,
                new MapSqlParameterSource()
                        .addValue("id", accountId)
                        .addValue("email", request.getEmail().trim().toLowerCase(Locale.ROOT))
                        .addValue("passwordHash", passwordEncoder.encode(request.getPassword()))
                        .addValue("firstName", nameParts[0])
                        .addValue("lastName", nameParts[1])
                        .addValue("phone", request.getPhone())
                        .addValue("dateOfBirth", request.getDob()));
        jdbcTemplate.update("insert into account_roles (account_id, role_id) values (:accountId, :roleId)",
                new MapSqlParameterSource().addValue("accountId", accountId).addValue("roleId", CUSTOMER_ROLE_ID));
        createEmailVerificationToken(accountId);

        return new RegistrationResponse(accountId, request.getEmail().trim().toLowerCase(Locale.ROOT), false);
    }

    @Transactional
    public TokenIssue login(LoginRequest request, HttpServletRequest httpRequest) {
        AccountRow account = findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (account.passwordHash() == null || !passwordEncoder.matches(request.getPassword(), account.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!account.emailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email verification is required before login");
        }

        return issueTokenPair(account.id(), account.email(), httpRequest);
    }

    @Transactional
    public TokenIssue refresh(String refreshToken, HttpServletRequest request) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }

        UUID accountId;
        String oldJti;
        try {
            accountId = jwtUtils.getAccountIdFromRefreshToken(refreshToken);
            oldJti = jwtUtils.getJtiFromToken(refreshToken);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        SessionRow session = findActiveSession(oldJti)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh session not found"));
        if (!session.accountId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh session mismatch");
        }

        if (!sha256Equals(refreshToken, session.refreshTokenHash())) {
            revokeAllSessions(accountId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token reuse detected");
        }

        AccountRow account = findAccountById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        revokeSession(oldJti);
        return issueTokenPair(account.id(), account.email(), request);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                revokeSession(jwtUtils.getJtiFromToken(refreshToken));
            } catch (Exception ignored) {
                log.debug("Ignoring invalid refresh token during logout");
            }
        }
    }

    @Transactional
    public void logoutAll() {
        revokeAllSessions(currentAccountId());
    }

    public List<AuthSessionResponse> sessions() {
        return jdbcTemplate.query("""
                        select jti, provider, ip_address, user_agent, created_at, expires_at, revoked_at
                        from auth_sessions
                        where account_id = :accountId
                        order by created_at desc
                        """,
                new MapSqlParameterSource("accountId", currentAccountId()),
                (rs, rowNum) -> new AuthSessionResponse(
                        rs.getString("jti"),
                        rs.getString("provider"),
                        rs.getString("ip_address"),
                        rs.getString("user_agent"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("expires_at").toInstant(),
                        rs.getTimestamp("revoked_at") == null ? null : rs.getTimestamp("revoked_at").toInstant()));
    }

    @Transactional
    public void verifyEmail(String token) {
        TokenRow row = findToken("email_verification_tokens", token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token"));
        jdbcTemplate.update("update email_verification_tokens set used_at = now() where id = :id",
                new MapSqlParameterSource("id", row.id()));
        jdbcTemplate.update("update accounts set email_verified = true, updated_at = now() where id = :accountId",
                new MapSqlParameterSource("accountId", row.accountId()));
    }

    @Transactional
    public void resendVerification(EmailRequest request) {
        Optional<AccountRow> account = findAccountByEmail(request.getEmail());
        if (account.isEmpty() || account.get().emailVerified()) {
            return;
        }

        int recent = jdbcTemplate.queryForObject("""
                        select count(*) from email_verification_tokens
                        where account_id = :accountId and used_at is null and created_at > now() - interval '60 seconds'
                        """,
                new MapSqlParameterSource("accountId", account.get().id()), Integer.class);
        if (recent > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification email was sent recently");
        }
        jdbcTemplate.update("delete from email_verification_tokens where account_id = :accountId and used_at is null",
                new MapSqlParameterSource("accountId", account.get().id()));
        createEmailVerificationToken(account.get().id());
    }

    @Transactional
    public void forgotPassword(EmailRequest request) {
        findAccountByEmail(request.getEmail()).ifPresent(account -> {
            String token = randomToken();
            jdbcTemplate.update("""
                            insert into password_reset_tokens (id, account_id, token_hash, expires_at)
                            values (:id, :accountId, :tokenHash, now() + interval '30 minutes')
                            """,
                    new MapSqlParameterSource()
                            .addValue("id", UUID.randomUUID())
                            .addValue("accountId", account.id())
                            .addValue("tokenHash", sha256(token)));
            log.info("Password reset link for {}: /reset-password?token={}", account.email(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String password = request.resolvedPassword();
        validatePassword(password, request.getConfirmPassword());
        TokenRow row = findToken("password_reset_tokens", request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));
        jdbcTemplate.update("update password_reset_tokens set used_at = now() where id = :id",
                new MapSqlParameterSource("id", row.id()));
        jdbcTemplate.update("update accounts set password_hash = :passwordHash, updated_at = now() where id = :accountId",
                new MapSqlParameterSource().addValue("passwordHash", passwordEncoder.encode(password)).addValue("accountId", row.accountId()));
        revokeAllSessions(row.accountId());
    }

    public CurrentUserResponse currentUser() {
        return toCurrentUser(findAccountById(currentAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")));
    }

    @Transactional
    public CurrentUserResponse updateCurrentUser(UserUpdateRequest request) {
        String[] nameParts = splitName(request.getFullName());
        jdbcTemplate.update("""
                        update accounts
                        set first_name = :firstName, last_name = :lastName, phone = :phone, date_of_birth = :dateOfBirth, updated_at = now()
                        where id = :accountId
                        """,
                new MapSqlParameterSource()
                        .addValue("accountId", currentAccountId())
                        .addValue("firstName", nameParts[0])
                        .addValue("lastName", nameParts[1])
                        .addValue("phone", request.getPhone())
                        .addValue("dateOfBirth", request.getDob()));
        return currentUser();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        AccountRow account = findAccountById(currentAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!passwordEncoder.matches(request.resolvedCurrentPassword(), account.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is not correct");
        }
        validatePassword(request.getNewPassword(), request.getConfirmPassword() == null ? request.getNewPassword() : request.getConfirmPassword());
        jdbcTemplate.update("update accounts set password_hash = :passwordHash, updated_at = now() where id = :accountId",
                new MapSqlParameterSource().addValue("passwordHash", passwordEncoder.encode(request.getNewPassword())).addValue("accountId", account.id()));
        revokeAllSessions(account.id());
    }

    public PageResponse<UserListItem> users(int limit, int offset) {
        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        int boundedOffset = Math.max(offset, 0);
        List<AccountRow> accounts = jdbcTemplate.query("""
                        select id, email, password_hash, first_name, last_name, email_verified, avatar_url, created_at, updated_at
                        from accounts
                        order by created_at desc
                        limit :limit offset :offset
                        """,
                new MapSqlParameterSource().addValue("limit", boundedLimit).addValue("offset", boundedOffset),
                accountMapper());
        Long total = jdbcTemplate.queryForObject("select count(*) from accounts", new MapSqlParameterSource(), Long.class);
        List<UserListItem> data = accounts.stream()
                .map(account -> new UserListItem(account.id(), account.firstName(), account.lastName(), account.email(), account.emailVerified(), account.createdAt(), account.updatedAt(), loadRoles(account.id())))
                .toList();
        return new PageResponse<>(data, new PageMeta(boundedLimit, boundedOffset, total == null ? 0 : total));
    }

    @Transactional
    public UserListItem updateUser(UUID userId, AdminUserUpdateRequest request) {
        assertExists("accounts", userId, "Account not found");
        jdbcTemplate.update("""
                        update accounts
                        set first_name = coalesce(:firstName, first_name),
                            last_name = coalesce(:lastName, last_name),
                            phone = coalesce(:phone, phone),
                            date_of_birth = coalesce(:dateOfBirth, date_of_birth),
                            updated_at = now()
                        where id = :accountId
                        """,
                new MapSqlParameterSource()
                        .addValue("accountId", userId)
                        .addValue("firstName", blankToNull(request.firstName()))
                        .addValue("lastName", blankToNull(request.lastName()))
                        .addValue("phone", blankToNull(request.phone()))
                        .addValue("dateOfBirth", request.dob()));
        AccountRow account = findAccountById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return new UserListItem(account.id(), account.firstName(), account.lastName(), account.email(), account.emailVerified(), account.createdAt(), account.updatedAt(), loadRoles(account.id()));
    }

    @Transactional
    public void assignRoles(RoleAssignmentRequest request) {
        assertExists("accounts", request.getUserId(), "Account not found");
        if (new HashSet<>(request.getRoleIds()).size() != request.getRoleIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate role ids are not allowed");
        }
        for (UUID roleId : request.getRoleIds()) {
            assertExists("roles", roleId, "Role not found");
        }
        jdbcTemplate.update("delete from account_roles where account_id = :accountId",
                new MapSqlParameterSource("accountId", request.getUserId()));
        for (UUID roleId : request.getRoleIds()) {
            jdbcTemplate.update("insert into account_roles (account_id, role_id) values (:accountId, :roleId)",
                    new MapSqlParameterSource().addValue("accountId", request.getUserId()).addValue("roleId", roleId));
        }
    }

    public PageResponse<RoleDto> roles(int limit, int offset) {
        return paged("roles", limit, offset, this::loadRolesPage);
    }

    public PageResponse<PermissionDto> permissions(int limit, int offset) {
        return paged("permissions", limit, offset, this::loadPermissionsPage);
    }

    public PageResponse<ActionDto> actions(int limit, int offset) {
        return paged("api_actions", limit, offset, this::loadActionsPage);
    }

    @Transactional
    public RoleDto createRole(RoleMutationRequest request) {
        UUID roleId = UUID.randomUUID();
        jdbcTemplate.update("""
                        insert into roles (id, name, display_name, description, is_system)
                        values (:id, :name, :displayName, :description, false)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", roleId)
                        .addValue("name", normalizeKey(request.getName()))
                        .addValue("displayName", request.getDisplayName() == null || request.getDisplayName().isBlank() ? request.getName().trim() : request.getDisplayName().trim())
                        .addValue("description", request.getDescription()));
        return loadRole(roleId);
    }

    @Transactional
    public RoleDto updateRole(UUID roleId, RoleMutationRequest request) {
        assertMutable("roles", roleId, "Role not found");
        jdbcTemplate.update("""
                        update roles
                        set name = :name, display_name = :displayName, description = :description, updated_at = now()
                        where id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", roleId)
                        .addValue("name", normalizeKey(request.getName()))
                        .addValue("displayName", request.getDisplayName() == null || request.getDisplayName().isBlank() ? request.getName().trim() : request.getDisplayName().trim())
                        .addValue("description", request.getDescription()));
        return loadRole(roleId);
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        assertMutable("roles", roleId, "Role not found");
        jdbcTemplate.update("delete from roles where id = :id", new MapSqlParameterSource("id", roleId));
    }

    @Transactional
    public PermissionDto createPermission(PermissionMutationRequest request) {
        UUID permissionId = UUID.randomUUID();
        jdbcTemplate.update("""
                        insert into permissions (id, key, name, description, is_system)
                        values (:id, :key, :name, :description, false)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", permissionId)
                        .addValue("key", normalizePermissionKey(request.getKey()))
                        .addValue("name", request.getName().trim())
                        .addValue("description", request.getDescription()));
        return loadPermission(permissionId);
    }

    @Transactional
    public PermissionDto updatePermission(UUID permissionId, PermissionMutationRequest request) {
        assertMutable("permissions", permissionId, "Permission not found");
        jdbcTemplate.update("""
                        update permissions
                        set key = :key, name = :name, description = :description, updated_at = now()
                        where id = :id
                        """,
                new MapSqlParameterSource()
                        .addValue("id", permissionId)
                        .addValue("key", normalizePermissionKey(request.getKey()))
                        .addValue("name", request.getName().trim())
                        .addValue("description", request.getDescription()));
        return loadPermission(permissionId);
    }

    @Transactional
    public void deletePermission(UUID permissionId) {
        assertMutable("permissions", permissionId, "Permission not found");
        jdbcTemplate.update("delete from permissions where id = :id", new MapSqlParameterSource("id", permissionId));
    }

    @Transactional
    public RoleDto replaceRolePermissions(UUID roleId, PermissionAssignmentRequest request) {
        assertMutable("roles", roleId, "Role not found");
        replacePermissionSet("role_permissions", "role_id", roleId, request.getPermissionIds());
        return loadRole(roleId);
    }

    @Transactional
    public ActionDto replaceActionPermissions(UUID actionId, PermissionAssignmentRequest request) {
        assertMutable("api_actions", actionId, "Action not found");
        if (new HashSet<>(request.getPermissionIds()).size() != request.getPermissionIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate permission ids are not allowed");
        }
        for (UUID permissionId : request.getPermissionIds()) {
            assertExists("permissions", permissionId, "Permission not found");
        }
        jdbcTemplate.update("delete from action_policies where action_id = :actionId",
                new MapSqlParameterSource("actionId", actionId));
        for (UUID permissionId : request.getPermissionIds()) {
            jdbcTemplate.update("""
                            insert into action_policies (id, action_id, permission_id, scope, mode)
                            values (:id, :actionId, :permissionId, 'GLOBAL', 'ANY')
                            """,
                    new MapSqlParameterSource().addValue("id", UUID.randomUUID()).addValue("actionId", actionId).addValue("permissionId", permissionId));
        }
        return loadAction(actionId);
    }

    public UUID currentAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountAuthUser accountAuthUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        return accountAuthUser.getAccountId();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private TokenIssue issueTokenPair(UUID accountId, String email, HttpServletRequest request) {
        String refreshJti = UUID.randomUUID().toString();
        Instant refreshExpiresAt = jwtUtils.getRefreshExpiresAt();
        String refreshToken = jwtUtils.generateRefreshToken(accountId, refreshJti, refreshExpiresAt);
        jdbcTemplate.update("""
                        insert into auth_sessions (id, account_id, jti, refresh_token_hash, provider, ip_address, user_agent, expires_at)
                        values (:id, :accountId, :jti, :refreshTokenHash, 'LOCAL', :ip, :userAgent, :expiresAt)
                        """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("accountId", accountId)
                        .addValue("jti", refreshJti)
                        .addValue("refreshTokenHash", sha256(refreshToken))
                        .addValue("ip", clientIp(request))
                        .addValue("userAgent", request == null ? null : request.getHeader("User-Agent"))
                        .addValue("expiresAt", Date.from(refreshExpiresAt)));
        String accessJti = UUID.randomUUID().toString();
        String accessToken = jwtUtils.generateAccessToken(email, accessJti);
        return new TokenIssue(new TokenResponse(accessToken, accessJti, "Bearer"), refreshToken, refreshExpiresAt);
    }

    private CurrentUserResponse toCurrentUser(AccountRow account) {
        return new CurrentUserResponse(
                account.id(),
                account.firstName(),
                account.lastName(),
                account.email(),
                account.phone(),
                account.dateOfBirth(),
                account.dateOfBirth(),
                loadRoles(account.id()),
                account.emailVerified(),
                account.createdAt(),
                account.updatedAt(),
                account.avatarUrl() == null ? null : new AvatarDto(account.avatarUrl()),
                loadAllowedActions(account.id()));
    }

    private List<String> loadAllowedActions(UUID accountId) {
        return jdbcTemplate.queryForList("""
                        select distinct aa.key
                        from account_roles ar
                        join role_permissions rp on rp.role_id = ar.role_id
                        join action_policies ap on ap.permission_id = rp.permission_id
                        join api_actions aa on aa.id = ap.action_id
                        where ar.account_id = :accountId and aa.enabled = true
                        order by aa.key
                        """,
                new MapSqlParameterSource("accountId", accountId), String.class);
    }

    private List<RoleDto> loadRoles(UUID accountId) {
        return jdbcTemplate.query("""
                        select r.id
                        from account_roles ar
                        join roles r on r.id = ar.role_id
                        where ar.account_id = :accountId
                        order by r.name
                        """,
                new MapSqlParameterSource("accountId", accountId),
                (rs, rowNum) -> loadRole((UUID) rs.getObject("id")));
    }

    private RoleDto loadRole(UUID roleId) {
        return jdbcTemplate.queryForObject("""
                        select id, name, display_name, description, is_system
                        from roles
                        where id = :id
                        """,
                new MapSqlParameterSource("id", roleId),
                (rs, rowNum) -> new RoleDto(
                        (UUID) rs.getObject("id"),
                        rs.getString("name"),
                        rs.getString("display_name"),
                        rs.getString("description"),
                        rs.getBoolean("is_system"),
                        loadRolePermissions((UUID) rs.getObject("id"))));
    }

    private PermissionDto loadPermission(UUID permissionId) {
        return jdbcTemplate.queryForObject("""
                        select id, key, name, description, is_system
                        from permissions
                        where id = :id
                        """,
                new MapSqlParameterSource("id", permissionId),
                (rs, rowNum) -> permissionFromRow(rs));
    }

    private ActionDto loadAction(UUID actionId) {
        return jdbcTemplate.queryForObject("""
                        select id, key, http_method, path, description, enabled, is_system
                        from api_actions
                        where id = :id
                        """,
                new MapSqlParameterSource("id", actionId),
                (rs, rowNum) -> {
                    List<ActionPolicyDto> policies = loadActionPolicies((UUID) rs.getObject("id"));
                    String mode = policies.isEmpty() ? "ANY" : policies.getFirst().mode();
                    return new ActionDto((UUID) rs.getObject("id"), rs.getString("key"), rs.getString("http_method"), rs.getString("path"), rs.getString("description"), rs.getBoolean("enabled"), rs.getBoolean("is_system"), mode, policies);
                });
    }

    private List<RolePermissionDto> loadRolePermissions(UUID roleId) {
        return jdbcTemplate.query("""
                        select p.id, p.key, p.name, p.description, p.is_system
                        from role_permissions rp
                        join permissions p on p.id = rp.permission_id
                        where rp.role_id = :roleId
                        order by p.key
                        """,
                new MapSqlParameterSource("roleId", roleId),
                (rs, rowNum) -> new RolePermissionDto((UUID) rs.getObject("id"), permissionFromRow(rs)));
    }

    private List<ActionPolicyDto> loadActionPolicies(UUID actionId) {
        return jdbcTemplate.query("""
                        select ap.id, ap.permission_id, ap.scope, ap.mode, p.id as p_id, p.key, p.name, p.description, p.is_system
                        from action_policies ap
                        join permissions p on p.id = ap.permission_id
                        where ap.action_id = :actionId
                        order by p.key
                        """,
                new MapSqlParameterSource("actionId", actionId),
                (rs, rowNum) -> new ActionPolicyDto((UUID) rs.getObject("id"), (UUID) rs.getObject("permission_id"), rs.getString("scope"), rs.getString("mode"),
                        new PermissionDto((UUID) rs.getObject("p_id"), rs.getString("key"), rs.getString("name"), rs.getString("description"), rs.getBoolean("is_system"))));
    }

    private <T> PageResponse<T> paged(String table, int limit, int offset, PageLoader<T> loader) {
        int boundedLimit = Math.min(Math.max(limit, 1), 100);
        int boundedOffset = Math.max(offset, 0);
        Long total = jdbcTemplate.queryForObject("select count(*) from " + table, new MapSqlParameterSource(), Long.class);
        return new PageResponse<>(loader.load(boundedLimit, boundedOffset), new PageMeta(boundedLimit, boundedOffset, total == null ? 0 : total));
    }

    private List<RoleDto> loadRolesPage(int limit, int offset) {
        return jdbcTemplate.query("select id from roles order by name limit :limit offset :offset",
                new MapSqlParameterSource().addValue("limit", limit).addValue("offset", offset),
                (rs, rowNum) -> loadRole((UUID) rs.getObject("id")));
    }

    private List<PermissionDto> loadPermissionsPage(int limit, int offset) {
        return jdbcTemplate.query("select id, key, name, description, is_system from permissions order by key limit :limit offset :offset",
                new MapSqlParameterSource().addValue("limit", limit).addValue("offset", offset),
                (rs, rowNum) -> permissionFromRow(rs));
    }

    private List<ActionDto> loadActionsPage(int limit, int offset) {
        return jdbcTemplate.query("select id from api_actions order by key limit :limit offset :offset",
                new MapSqlParameterSource().addValue("limit", limit).addValue("offset", offset),
                (rs, rowNum) -> loadAction((UUID) rs.getObject("id")));
    }

    private PermissionDto permissionFromRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PermissionDto((UUID) rs.getObject("id"), rs.getString("key"), rs.getString("name"), rs.getString("description"), rs.getBoolean("is_system"));
    }

    private void replacePermissionSet(String table, String ownerColumn, UUID ownerId, List<UUID> permissionIds) {
        if (new HashSet<>(permissionIds).size() != permissionIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate permission ids are not allowed");
        }
        for (UUID permissionId : permissionIds) {
            assertExists("permissions", permissionId, "Permission not found");
        }
        jdbcTemplate.update("delete from " + table + " where " + ownerColumn + " = :ownerId",
                new MapSqlParameterSource("ownerId", ownerId));
        for (UUID permissionId : permissionIds) {
            jdbcTemplate.update("insert into " + table + " (" + ownerColumn + ", permission_id) values (:ownerId, :permissionId)",
                    new MapSqlParameterSource().addValue("ownerId", ownerId).addValue("permissionId", permissionId));
        }
    }

    private void assertMutable(String table, UUID id, String notFoundMessage) {
        List<Boolean> rows = jdbcTemplate.queryForList("select is_system from " + table + " where id = :id",
                new MapSqlParameterSource("id", id), Boolean.class);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFoundMessage);
        }
        if (Boolean.TRUE.equals(rows.getFirst())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "System authorization entries cannot be edited");
        }
    }

    private void assertExists(String table, UUID id, String message) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from " + table + " where id = :id",
                new MapSqlParameterSource("id", id), Integer.class);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
    }

    private void createEmailVerificationToken(UUID accountId) {
        String token = randomToken();
        jdbcTemplate.update("""
                        insert into email_verification_tokens (id, account_id, token_hash, expires_at)
                        values (:id, :accountId, :tokenHash, now() + interval '30 minutes')
                        """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("accountId", accountId)
                        .addValue("tokenHash", sha256(token)));
        log.info("Email verification link for account {}: /verify-email?token={}", accountId, token);
    }

    private Optional<TokenRow> findToken(String table, String token) {
        List<TokenRow> rows = jdbcTemplate.query("""
                        select id, account_id
                        from %s
                        where token_hash = :tokenHash and used_at is null and expires_at > now()
                        """.formatted(table),
                new MapSqlParameterSource("tokenHash", sha256(token)),
                (rs, rowNum) -> new TokenRow((UUID) rs.getObject("id"), (UUID) rs.getObject("account_id")));
        return rows.stream().findFirst();
    }

    private Optional<AccountRow> findAccountByEmail(String email) {
        List<AccountRow> rows = jdbcTemplate.query("""
                        select id, email, password_hash, first_name, last_name, phone, date_of_birth, email_verified, avatar_url, created_at, updated_at
                        from accounts
                        where lower(email) = lower(:email)
                        """,
                new MapSqlParameterSource("email", email), accountMapper());
        return rows.stream().findFirst();
    }

    private Optional<AccountRow> findAccountById(UUID id) {
        List<AccountRow> rows = jdbcTemplate.query("""
                        select id, email, password_hash, first_name, last_name, phone, date_of_birth, email_verified, avatar_url, created_at, updated_at
                        from accounts
                        where id = :id
                        """,
                new MapSqlParameterSource("id", id), accountMapper());
        return rows.stream().findFirst();
    }

    private RowMapper<AccountRow> accountMapper() {
        return (rs, rowNum) -> new AccountRow(
                (UUID) rs.getObject("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getObject("date_of_birth", java.time.LocalDate.class),
                rs.getBoolean("email_verified"),
                rs.getString("avatar_url"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant());
    }

    private Optional<SessionRow> findActiveSession(String jti) {
        List<SessionRow> rows = jdbcTemplate.query("""
                        select account_id, refresh_token_hash
                        from auth_sessions
                        where jti = :jti and revoked_at is null and expires_at > now()
                        """,
                new MapSqlParameterSource("jti", jti),
                (rs, rowNum) -> new SessionRow((UUID) rs.getObject("account_id"), rs.getString("refresh_token_hash")));
        return rows.stream().findFirst();
    }

    private void revokeSession(String jti) {
        jdbcTemplate.update("update auth_sessions set revoked_at = coalesce(revoked_at, now()) where jti = :jti",
                new MapSqlParameterSource("jti", jti));
    }

    private void revokeAllSessions(UUID accountId) {
        jdbcTemplate.update("update auth_sessions set revoked_at = coalesce(revoked_at, now()) where account_id = :accountId and revoked_at is null",
                new MapSqlParameterSource("accountId", accountId));
    }

    private boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from accounts where lower(email) = lower(:email)",
                new MapSqlParameterSource("email", email), Integer.class);
        return count != null && count > 0;
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password confirmation does not match");
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
        }
    }

    private String[] splitName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            return new String[]{"Account", ""};
        }
        int split = normalized.indexOf(' ');
        if (split < 0) {
            return new String[]{normalized, ""};
        }
        return new String[]{normalized.substring(0, split), normalized.substring(split + 1)};
    }

    private String normalizeKey(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]+", "_");
    }

    private String normalizePermissionKey(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String randomToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private boolean sha256Equals(String value, String expectedHash) {
        if (expectedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(
                sha256(value).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record AccountRow(UUID id, String email, String passwordHash, String firstName, String lastName, String phone, java.time.LocalDate dateOfBirth, boolean emailVerified, String avatarUrl, Instant createdAt, Instant updatedAt) {
    }

    private record SessionRow(UUID accountId, String refreshTokenHash) {
    }

    private record TokenRow(UUID id, UUID accountId) {
    }

    @FunctionalInterface
    private interface PageLoader<T> {
        List<T> load(int limit, int offset);
    }
}
