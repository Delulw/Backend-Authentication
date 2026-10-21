package com.delulw.backendauthentication.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "server.ssl.enabled=false",
        "server.ssl.key-store=classpath:application.yml",
        "server.ssl.key-store-type=PKCS12",
        "server.ssl.key-store-password=test-only"
})
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsAuthenticationEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsProtectedEndpointsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized());
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
