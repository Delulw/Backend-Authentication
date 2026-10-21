package com.delulw.backendauthentication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "security.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.jwt.expiration-ms=900000",
        "security.auth.username=test-user",
        "security.auth.password=test-password",
        "security.auth.roles=USER",
        "server.ssl.enabled=false",
        "server.ssl.key-store=classpath:application.yml",
        "server.ssl.key-store-type=PKCS12",
        "server.ssl.key-store-password=test-only"
})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void permitsAuthenticationEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsProtectedEndpointsWithoutTokenWithJsonError() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void permitsAuthenticatedUsersOnProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectsUsersWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/status"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void permitsAdministratorsOnAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/status"))
                .andExpect(status().isOk());
    }

    @Test
    void loginReturnsTokenThatAuthenticatesProtectedRequests() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-user\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(response).path("accessToken").asText();
        Assertions.assertFalse(token.isBlank());

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidLoginWithJsonError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test-user\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void rejectsInvalidBearerTokenWithJsonError() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(roles = "USER")
    void forbiddenResponseContainsJsonError() throws Exception {
        mockMvc.perform(get("/api/admin/status"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message")
                        .value("You do not have permission to access this resource"));
    }

    @RestController
    static class SecurityTestController {

        @GetMapping({"/api/auth/status", "/api/test/protected", "/api/admin/status"})
        String status() {
            return "ok";
        }
    }

    @TestConfiguration
    static class SecurityTestControllerConfiguration {

        @Bean
        SecurityTestController securityTestController() {
            return new SecurityTestController();
        }
    }
}
