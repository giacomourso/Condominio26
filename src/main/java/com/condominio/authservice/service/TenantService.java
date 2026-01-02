package com.condominio.authservice.service;

import com.condominio.authservice.domain.Tenant;
import com.condominio.authservice.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public TenantService(TenantRepository tenantRepository, AuditService auditService) {
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tenant getById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
    }

    @Transactional
    public Tenant create(String name) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        Tenant saved = tenantRepository.save(tenant);
        auditService.recordAction("Tenant", saved.getId().toString(), "CREATE", "created tenant");
        return saved;
    }

    @Transactional
    public Tenant update(UUID id, String name) {
        Tenant tenant = getById(id);
        tenant.setName(name);
        Tenant saved = tenantRepository.save(tenant);
        auditService.recordAction("Tenant", saved.getId().toString(), "UPDATE", "updated tenant");
        return saved;
    }
}
