package com.condominio.authservice.service;

import com.condominio.authservice.audit.AuditLog;
import com.condominio.authservice.repository.AuditLogRepository;
import com.condominio.authservice.security.TenantContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void recordAction(String entityType, String entityId, String action, String metadata) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setPerformedBy(resolveUser());
        log.setTenantId(TenantContextHolder.getTenantId());
        log.setMetadata(metadata);
        auditLogRepository.save(log);
    }

    private String resolveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
