package com.condominio.authservice.web.dto;

import java.time.Instant;
import java.util.UUID;

public class RoleResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID tenantId;
    private Instant createdAt;
    private Instant updatedAt;

    public RoleResponse(UUID id, String name, String description, UUID tenantId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tenantId = tenantId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
