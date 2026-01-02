package com.condominio.authservice.repository;

import com.condominio.authservice.domain.Tenant;
import com.condominio.authservice.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    List<UserAccount> findByTenant(Tenant tenant);
}
