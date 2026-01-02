package com.condominio.authservice.web.controller;

import com.condominio.authservice.domain.Tenant;
import com.condominio.authservice.service.TenantService;
import com.condominio.authservice.web.dto.TenantRequest;
import com.condominio.authservice.web.dto.TenantResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<TenantResponse> list() {
        return tenantService.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public TenantResponse get(@PathVariable String tenantId) {
        Tenant tenant = tenantService.getById(UUID.fromString(tenantId));
        return toResponse(tenant);
    }

    @PostMapping
    public TenantResponse create(@Valid @RequestBody TenantRequest request) {
        Tenant tenant = tenantService.create(request.getName());
        return toResponse(tenant);
    }

    @PutMapping("/{tenantId}")
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public TenantResponse update(@PathVariable String tenantId, @Valid @RequestBody TenantRequest request) {
        Tenant tenant = tenantService.update(UUID.fromString(tenantId), request.getName());
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getCreatedAt(), tenant.getUpdatedAt());
    }
}
