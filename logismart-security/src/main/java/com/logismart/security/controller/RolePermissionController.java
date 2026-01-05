package com.logismart.security.controller;

import com.logismart.security.dto.PermissionResponse;
import com.logismart.security.entity.Permission;
import com.logismart.security.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for managing permissions assigned to roles
 * PRD Compliance: "Assigner / retirer une permission à un rôle"
 * PRD Compliance: "Consulter les permissions d'un rôle"
 */
@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Role Permissions", description = "Admin APIs for managing role permissions (ADMIN only)")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    /**
     * Assign a permission to a role
     * PRD: "Assigner une permission à un rôle"
     *
     * @param roleId       ID of the role
     * @param permissionId ID of the permission to assign
     * @return 200 OK
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Assign permission to role", description = "Assign a permission to a role. All users with this role will inherit the permission.")
    public ResponseEntity<Void> assignPermissionToRole(
            @PathVariable String roleId,
            @PathVariable String permissionId) {
        log.info("REST request to assign permission {} to role {}", permissionId, roleId);
        rolePermissionService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Revoke a permission from a role
     * PRD: "Retirer une permission d'un rôle"
     *
     * @param roleId       ID of the role
     * @param permissionId ID of the permission to revoke
     * @return 204 No Content
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Revoke permission from role", description = "Revoke a permission from a role. Users with this role will lose this permission.")
    public ResponseEntity<Void> revokePermissionFromRole(
            @PathVariable String roleId,
            @PathVariable String permissionId) {
        log.info("REST request to revoke permission {} from role {}", permissionId, roleId);
        rolePermissionService.revokePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all permissions assigned to a role
     * PRD: "Consulter les permissions d'un rôle"
     *
     * @param roleId ID of the role
     * @return Set of permissions
     */
    @GetMapping("/{roleId}/permissions")
    @Operation(summary = "Get role permissions", description = "Get all permissions assigned to a role")
    public ResponseEntity<Set<PermissionResponse>> getRolePermissions(@PathVariable String roleId) {
        log.info("REST request to get permissions for role {}", roleId);
        Set<Permission> permissions = rolePermissionService.getRolePermissions(roleId);

        Set<PermissionResponse> response = permissions.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(response);
    }

    /**
     * Convert Permission entity to PermissionResponse DTO
     */
    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource())
                .action(permission.getAction())
                .enabled(permission.getEnabled())
                .build();
    }
}
