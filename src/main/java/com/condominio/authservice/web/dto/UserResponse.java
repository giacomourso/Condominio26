package com.condominio.authservice.web.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private UUID tenantId;
    private Set<UUID> roleIds;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse(UUID id, String username, String email, UUID tenantId, Set<UUID> roleIds, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.tenantId = tenantId;
        this.roleIds = roleIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Set<UUID> getRoleIds() {
        return roleIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
