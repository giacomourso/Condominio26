package com.condominio.authservice.security;

public class TenantGuard {

    public boolean isSameTenant(String tenantId) {
        String contextTenant = TenantContextHolder.getTenantId();
        return contextTenant != null && contextTenant.equals(tenantId);
    }
}
