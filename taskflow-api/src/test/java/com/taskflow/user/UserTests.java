package com.taskflow.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UserTests {

    @Test 
    void startsUnverified() {
        User user = new User(
            "person@example.com",
            "$2a$12$hash"
        );

        assertThat(user.isEmailVerified()).isFalse();

        user.verifyEmail();
        
        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test 
    void passwordChangeAdvancesCredentialVersion() {
        User user = new User(
            "person@example.com",
            "$2a$12$hash"
        );
        
        user.changePasswordHash("$2a$12$newhash");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$12$newhash");
        assertThat(user.getCredentialVersion()).isEqualTo(2);
    }
}
