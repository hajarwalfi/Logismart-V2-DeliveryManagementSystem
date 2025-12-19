package com.logismart.security.controller;

import com.logismart.security.dto.PermissionRequest;
import com.logismart.security.dto.PermissionResponse;
import com.logismart.security.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller for managing permissions
 * Only accessible by ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Permissions", description = "Admin APIs for managing permissions (ADMIN only)")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(
            summary = "Create a new permission",
            description = "Creates a new permission with specified name, resource, and action. " +
                    "Permission names must be unique."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Permission created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or permission already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody PermissionRequest request) {
        log.info("REST: Admin creating permission: {}", request.getName());
        PermissionResponse created = permissionService.createPermission(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get all permissions",
            description = "Retrieves all permissions in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        log.info("REST: Admin retrieving all permissions");
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get permission by ID",
            description = "Retrieves a specific permission by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission found"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<PermissionResponse> getPermissionById(
            @Parameter(description = "Permission ID", required = true)
            @PathVariable String id) {
        log.info("REST: Admin retrieving permission: {}", id);
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @GetMapping("/resource/{resource}")
    @Operation(
            summary = "Get permissions by resource",
            description = "Retrieves all permissions for a specific resource (e.g., PARCEL, ZONE)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<List<PermissionResponse>> getPermissionsByResource(
            @Parameter(description = "Resource name", required = true)
            @PathVariable String resource) {
        log.info("REST: Admin retrieving permissions for resource: {}", resource);
        List<PermissionResponse> permissions = permissionService.getPermissionsByResource(resource);
        return ResponseEntity.ok(permissions);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a permission",
            description = "Updates an existing permission's details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<PermissionResponse> updatePermission(
            @Parameter(description = "Permission ID", required = true)
            @PathVariable String id,
            @Valid @RequestBody PermissionRequest request) {
        log.info("REST: Admin updating permission: {}", id);
        PermissionResponse updated = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a permission",
            description = "Deletes a permission from the system. " +
                    "WARNING: This will remove the permission from all users who have it."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Permission deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "Permission ID", required = true)
            @PathVariable String id) {
        log.warn("REST: Admin deleting permission: {}", id);
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search permissions",
            description = "Searches permissions by keyword in name or description"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    public ResponseEntity<List<PermissionResponse>> searchPermissions(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {
        log.info("REST: Admin searching permissions with keyword: {}", keyword);
        List<PermissionResponse> permissions = permissionService.searchPermissions(keyword);
        return ResponseEntity.ok(permissions);
    }
}
