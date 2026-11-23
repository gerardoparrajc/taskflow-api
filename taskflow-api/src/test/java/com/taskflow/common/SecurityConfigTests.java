package com.taskflow.common;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityConfigTests {

    private final PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

    @Test 
    void storesAndChecksPasswordHashes() {
        String password = "learn-safe-password";

        String hash = passwordEncoder.encode(password);

        assertThat(hash).startsWith("$2");
        assertThat(hash).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, hash)).isTrue();
        assertThat(passwordEncoder.matches("wrong-password", hash)).isFalse();
    }
}
