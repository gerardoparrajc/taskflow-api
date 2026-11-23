package com.taskflow.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "users")
public class User {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, unique = true)
    private String email;

    @Column (name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column (name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column (name = "credential_version", nullable = false)
    private long credentialVersion;

    @Column (name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.emailVerified = false;
        this.credentialVersion = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public long getCredentialVersion() {
        return credentialVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void verifyEmail() {
        emailVerified = true;
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        credentialVersion++;
    }

}
