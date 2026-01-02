package com.condominio.authservice.repository;

import com.condominio.authservice.domain.Role;
import com.condominio.authservice.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    List<Role> findByTenant(Tenant tenant);
}
