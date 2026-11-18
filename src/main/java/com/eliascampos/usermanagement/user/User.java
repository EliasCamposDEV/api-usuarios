package com.eliascampos.usermanagement.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "usuarios")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private UserRole role;

    private boolean active;

    // Versao do refresh token em vigor: cada refresh emitido carrega este numero e a rotacao o incrementa.
    private int tokenVersion;

    private Instant createdAt;

    private Instant updatedAt;

    protected User() {
        // Construtor exigido pelo Spring Data MongoDB.
    }

    public User(String name, String email, UserRole role, String passwordHash) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.passwordHash = passwordHash;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String name, String email, UserRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.updatedAt = Instant.now();
    }

    public void changeStatus(boolean active) {
        this.active = active;
        this.updatedAt = Instant.now();
    }

    public void rotateTokenVersion() {
        this.tokenVersion++;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
