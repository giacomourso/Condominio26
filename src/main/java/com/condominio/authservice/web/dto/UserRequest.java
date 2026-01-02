package com.condominio.authservice.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.UUID;

public class UserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private Set<UUID> roleIds;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UUID> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<UUID> roleIds) {
        this.roleIds = roleIds;
    }
}
