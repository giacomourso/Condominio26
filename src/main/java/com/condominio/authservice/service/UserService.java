package com.condominio.authservice.service;

import com.condominio.authservice.domain.Role;
import com.condominio.authservice.domain.Tenant;
import com.condominio.authservice.domain.UserAccount;
import com.condominio.authservice.repository.RoleRepository;
import com.condominio.authservice.repository.TenantRepository;
import com.condominio.authservice.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    public UserService(UserAccountRepository userAccountRepository,
                       TenantRepository tenantRepository,
                       RoleRepository roleRepository,
                       AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findByTenant(UUID tenantId) {
        Tenant tenant = findTenant(tenantId);
        return userAccountRepository.findByTenant(tenant);
    }

    @Transactional
    public UserAccount create(UUID tenantId, String username, String email, Set<UUID> roleIds) {
        Tenant tenant = findTenant(tenantId);
        Set<Role> roles = resolveRoles(roleIds, tenant);

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(username);
        userAccount.setEmail(email);
        userAccount.setTenant(tenant);
        userAccount.setRoles(roles);

        UserAccount saved = userAccountRepository.save(userAccount);
        auditService.recordAction("User", saved.getId().toString(), "CREATE", "created user");
        return saved;
    }

    @Transactional
    public UserAccount update(UUID tenantId, UUID userId, String username, String email, Set<UUID> roleIds) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (!userAccount.getTenant().getId().equals(tenantId)) {
            throw new IllegalArgumentException("User tenant mismatch");
        }

        Set<Role> roles = resolveRoles(roleIds, userAccount.getTenant());
        userAccount.setUsername(username);
        userAccount.setEmail(email);
        userAccount.setRoles(roles);

        UserAccount saved = userAccountRepository.save(userAccount);
        auditService.recordAction("User", saved.getId().toString(), "UPDATE", "updated user");
        return saved;
    }

    private Set<Role> resolveRoles(Set<UUID> roleIds, Tenant tenant) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        for (Role role : roles) {
            if (!role.getTenant().getId().equals(tenant.getId())) {
                throw new IllegalArgumentException("Role tenant mismatch");
            }
        }
        if (roles.size() != roleIds.size()) {
            throw new IllegalArgumentException("One or more roles not found");
        }
        return new HashSet<>(roles);
    }

    private Tenant findTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }
}
