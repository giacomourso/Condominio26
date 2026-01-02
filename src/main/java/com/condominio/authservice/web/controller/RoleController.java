package com.condominio.authservice.web.controller;

import com.condominio.authservice.domain.Role;
import com.condominio.authservice.service.RoleService;
import com.condominio.authservice.web.dto.RoleRequest;
import com.condominio.authservice.web.dto.RoleResponse;
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
@RequestMapping("/api/tenants/{tenantId}/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public List<RoleResponse> list(@PathVariable String tenantId) {
        UUID tenantUuid = UUID.fromString(tenantId);
        return roleService.findByTenant(tenantUuid)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public RoleResponse create(@PathVariable String tenantId, @Valid @RequestBody RoleRequest request) {
        Role role = roleService.create(UUID.fromString(tenantId), request.getName(), request.getDescription());
        return toResponse(role);
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public RoleResponse update(@PathVariable String tenantId,
                               @PathVariable String roleId,
                               @Valid @RequestBody RoleRequest request) {
        Role role = roleService.update(UUID.fromString(tenantId),
                UUID.fromString(roleId),
                request.getName(),
                request.getDescription());
        return toResponse(role);
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), role.getTenant().getId(),
                role.getCreatedAt(), role.getUpdatedAt());
    }
}
