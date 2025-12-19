package com.logismart.security.service;

import com.logismart.security.entity.Permission;
import com.logismart.security.entity.Role;
import com.logismart.security.exception.ResourceNotFoundException;
import com.logismart.security.repository.PermissionRepository;
import com.logismart.security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service for managing permissions assigned to roles
 * PRD Compliance: "Assigner / retirer une permission à un rôle"
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Assign a permission to a role
     * PRD: "Assigner une permission à un rôle"
     */
    @Transactional
    public void assignPermissionToRole(String roleId, String permissionId) {
        log.info("Assigning permission {} to role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.getPermissions().add(permission);
        roleRepository.save(role);

        log.info("Successfully assigned permission {} to role {}", permission.getName(), role.getName());
    }

    /**
     * Revoke a permission from a role
     * PRD: "Retirer une permission d'un rôle"
     */
    @Transactional
    public void revokePermissionFromRole(String roleId, String permissionId) {
        log.info("Revoking permission {} from role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        role.getPermissions().remove(permission);
        roleRepository.save(role);

        log.info("Successfully revoked permission {} from role {}", permission.getName(), role.getName());
    }

    /**
     * Get all permissions assigned to a role
     * PRD: "Consulter les permissions d'un rôle"
     */
    @Transactional(readOnly = true)
    public Set<Permission> getRolePermissions(String roleId) {
        log.info("Fetching permissions for role {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        return role.getPermissions();
    }
}
