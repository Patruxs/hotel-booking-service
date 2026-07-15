package org.example.hotelbookingservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacIdentityFlowIntegrationTest {
    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID OWNER_ROLE_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void rbacCatalogAndAssignmentFlowsPreservePublicResponseShapes() throws Exception {
        String adminEmail = "identity-rbac-admin-" + UUID.randomUUID() + "@example.com";
        String targetEmail = "identity-rbac-target-" + UUID.randomUUID() + "@example.com";
        UUID adminId = insertAccount(adminEmail, true);
        UUID targetId = insertAccount(targetEmail, true);
        assignRole(adminId, ADMIN_ROLE_ID);
        assignRole(targetId, OWNER_ROLE_ID);

        UUID ownedHotelId = UUID.randomUUID();
        UUID foreignOwnerId = insertAccount("identity-rbac-foreign-" + UUID.randomUUID() + "@example.com", true);
        UUID foreignHotelId = UUID.randomUUID();
        insertActiveHotel(ownedHotelId, targetId, "Owner-scoped hotel");
        insertActiveHotel(foreignHotelId, foreignOwnerId, "Foreign hotel");

        String accessToken = login(adminEmail);
        String ownerAccessToken = login(targetEmail);

        mockMvc.perform(get("/api/v1/hotels/manageable")
                        .header("Authorization", "Bearer " + ownerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meta.total").value(1))
                .andExpect(jsonPath("$.data.data[0].id").value(ownedHotelId.toString()))
                .andExpect(jsonPath("$.data.data[0].ownerId").value(targetId.toString()));

        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.meta.total").isNumber());

        mockMvc.perform(get("/api/v1/permissions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").isArray());

        mockMvc.perform(get("/api/v1/actions")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").isArray());

        JsonNode role = createRole(accessToken);
        JsonNode permission = createPermission(accessToken);
        UUID roleId = UUID.fromString(role.at("/data/id").asText());
        UUID permissionId = UUID.fromString(permission.at("/data/id").asText());

        mockMvc.perform(post("/api/v1/roles/{roleId}/permissions", roleId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("permissionIds", List.of(permissionId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.permissions[0].permissionId").value(permissionId.toString()));

        mockMvc.perform(post("/api/v1/roles/assign-to-user")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", targetId, "roleIds", List.of(roleId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        UUID actionId = insertMutableAction();
        mockMvc.perform(patch("/api/v1/actions/{actionId}/permissions", actionId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("permissionIds", List.of(permissionId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.policies[0].permissionId").value(permissionId.toString()));

        Integer policyCount = jdbc.queryForObject("select count(*) from action_policies", Integer.class);
        assertThat(policyCount).isPositive();
    }

    private UUID insertAccount(String email, boolean verified) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                insert into accounts (id, email, password_hash, first_name, last_name, email_verified, auth_provider)
                values (?, ?, ?, 'RBAC', 'User', ?, 'LOCAL')
                """, id, email, passwordEncoder.encode("Password1!"), verified);
        return id;
    }

    private void assignRole(UUID accountId, UUID roleId) {
        jdbc.update("insert into account_roles (account_id, role_id) values (?, ?)", accountId, roleId);
    }

    private void insertActiveHotel(UUID hotelId, UUID ownerId, String name) {
        jdbc.update("""
                insert into hotels (id, owner_id, name, slug, description, address, city, country, status, star_rating)
                values (?, ?, ?, ?, 'RBAC integration hotel', '1 Test Street', 'Da Nang', 'Vietnam', 'ACTIVE', 4.0)
                """, hotelId, ownerId, name, "rbac-" + hotelId);
    }

    private UUID insertMutableAction() {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                insert into api_actions (id, key, http_method, path, description, enabled, is_system)
                values (?, ?, 'GET', ?, 'Mutable RBAC integration action', true, false)
                """, id, "identity.test.action." + id, "/api/v1/identity-test/" + id);
        return id;
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).at("/data/accessToken").asText();
    }

    private JsonNode createRole(String accessToken) throws Exception {
        String name = "IDENTITY_TEST_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String body = mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", name,
                                "displayName", "Identity Test Role",
                                "description", "Identity alignment test role"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    private JsonNode createPermission(String accessToken) throws Exception {
        String key = "identity.test." + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String body = mockMvc.perform(post("/api/v1/permissions")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "key", key,
                                "name", "Identity Test Permission",
                                "description", "Identity alignment test permission"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
