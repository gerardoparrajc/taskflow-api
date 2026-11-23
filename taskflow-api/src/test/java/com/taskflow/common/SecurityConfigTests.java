package com.taskflow.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@WebMvcTest(controllers = SecurityConfigTests.TestController.class)
@Import ({
    SecurityConfig.class,
    SecurityConfigTests.TestController.class
})
public class SecurityConfigTests {

    private final MockMvc mockMvc;
    private final PasswordEncoder passwordEncoder;

    @Autowired 
    SecurityConfigTests(MockMvc mockMvc, PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.passwordEncoder = passwordEncoder;
    }

    @ParameterizedTest 
    @ValueSource (strings = {
        "/api/v1/auth/test",
        "/v3/api-docs/test",
        "/swagger-ui/test",
        "/swagger-ui.html"
    })
    void publicRoutesAreAccesibleWithoutAuthentication(String path) throws Exception {
        mockMvc.perform(get(path))
            .andExpect(status().isOk())
            .andExpect(content().string("ok"));
    }

    @Test
    void publicPostIsAccessibleWithoutAuthenticationOrCsrfToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("ok"));
    }

    @Test 
    void protectedRouteIsRejectedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser 
    void protectedRouteIsAccesibleWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
            .andExpect(status().isOk())
            .andExpect(content().string("ok"));
    }

    @Test 
    void storesAndChecksPasswordHashes() {
        String password = "learn-safe-password";

        String hash = passwordEncoder.encode(password);

        assertThat(hash).startsWith("$2");
        assertThat(hash).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, hash)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", hash)).isFalse();
    }

    @RestController 
    static class TestController {
        @GetMapping({
            "/api/v1/auth/test",
            "/v3/api-docs/test",
            "/swagger-ui/test",
            "/swagger-ui.html",
            "/api/v1/projects"
        })
        String getRoute() {
            return "ok";
        }
        
        @PostMapping ("/api/v1/auth/test")
        String postAuthRoute() {
            return "ok";
        }
        
    }
}
