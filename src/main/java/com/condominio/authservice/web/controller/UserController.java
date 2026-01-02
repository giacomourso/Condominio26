package com.condominio.authservice.web.controller;

import com.condominio.authservice.domain.UserAccount;
import com.condominio.authservice.service.UserService;
import com.condominio.authservice.web.dto.UserRequest;
import com.condominio.authservice.web.dto.UserResponse;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tenants/{tenantId}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public List<UserResponse> list(@PathVariable String tenantId) {
        UUID tenantUuid = UUID.fromString(tenantId);
        return userService.findByTenant(tenantUuid)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public UserResponse create(@PathVariable String tenantId, @Valid @RequestBody UserRequest request) {
        UserAccount userAccount = userService.create(UUID.fromString(tenantId),
                request.getUsername(),
                request.getEmail(),
                request.getRoleIds());
        return toResponse(userAccount);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@tenantGuard.isSameTenant(#tenantId)")
    public UserResponse update(@PathVariable String tenantId,
                               @PathVariable String userId,
                               @Valid @RequestBody UserRequest request) {
        UserAccount userAccount = userService.update(UUID.fromString(tenantId),
                UUID.fromString(userId),
                request.getUsername(),
                request.getEmail(),
                request.getRoleIds());
        return toResponse(userAccount);
    }

    private UserResponse toResponse(UserAccount userAccount) {
        Set<UUID> roleIds = userAccount.getRoles()
                .stream()
                .map(role -> role.getId())
                .collect(Collectors.toSet());
        return new UserResponse(userAccount.getId(), userAccount.getUsername(), userAccount.getEmail(),
                userAccount.getTenant().getId(), roleIds, userAccount.getCreatedAt(), userAccount.getUpdatedAt());
    }
}
