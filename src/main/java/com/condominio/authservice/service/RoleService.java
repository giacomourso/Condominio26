package com.condominio.authservice.service;

import com.condominio.authservice.domain.Role;
import com.condominio.authservice.domain.Tenant;
import com.condominio.authservice.repository.RoleRepository;
import com.condominio.authservice.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository, TenantRepository tenantRepository, AuditService auditService) {
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Role> findByTenant(UUID tenantId) {
        Tenant tenant = findTenant(tenantId);
        return roleRepository.findByTenant(tenant);
    }

    @Transactional
    public Role create(UUID tenantId, String name, String description) {
        Tenant tenant = findTenant(tenantId);

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setTenant(tenant);

        Role saved = roleRepository.save(role);
        auditService.recordAction("Role", saved.getId().toString(), "CREATE", "created role");
        return saved;
    }

    @Transactional
    public Role update(UUID tenantId, UUID roleId, String name, String description) {
        Role existing = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        if (!existing.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("Role tenant mismatch");
        }

        existing.setName(name);
        existing.setDescription(description);

        Role saved = roleRepository.save(existing);
        auditService.recordAction("Role", saved.getId().toString(), "UPDATE", "updated role");
        return saved;
    }

    private Tenant findTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }
}
