package com.logismart.security.service;

import com.logismart.security.dto.PermissionRequest;
import com.logismart.security.dto.PermissionResponse;
import com.logismart.security.entity.Permission;
import com.logismart.security.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionResponse createPermission(PermissionRequest request) {
        log.info("Creating permission: {}", request.getName());

        if (permissionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Permission with name '" + request.getName() + "' already exists");
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .resource(request.getResource())
                .action(request.getAction())
                .enabled(request.getEnabled())
                .build();

        Permission saved = permissionRepository.save(permission);
        log.info("Permission created: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(String id) {
        log.info("Finding permission by ID: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + id));
        return mapToResponse(permission);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        log.info("Finding all permissions");
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByResource(String resource) {
        log.info("Finding permissions for resource: {}", resource);
        return permissionRepository.findByResource(resource).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PermissionResponse updatePermission(String id, PermissionRequest request) {
        log.info("Updating permission: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with ID: " + id));

        // Check if name is being changed and if new name already exists
        if (!permission.getName().equals(request.getName()) &&
            permissionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Permission with name '" + request.getName() + "' already exists");
        }

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setResource(request.getResource());
        permission.setAction(request.getAction());
        permission.setEnabled(request.getEnabled());

        Permission updated = permissionRepository.save(permission);
        log.info("Permission updated: {}", updated.getId());

        return mapToResponse(updated);
    }

    public void deletePermission(String id) {
        log.warn("Deleting permission: {}", id);

        if (!permissionRepository.existsById(id)) {
            throw new IllegalArgumentException("Permission not found with ID: " + id);
        }

        permissionRepository.deleteById(id);
        log.info("Permission deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> searchPermissions(String keyword) {
        log.info("Searching permissions with keyword: {}", keyword);
        return permissionRepository.searchPermissions(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PermissionResponse mapToResponse(Permission permission) {
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
