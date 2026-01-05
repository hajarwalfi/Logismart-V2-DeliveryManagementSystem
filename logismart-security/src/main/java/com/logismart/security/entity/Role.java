package com.logismart.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity representing system roles
 * Each role has a set of permissions that define what actions users with this role can perform
 * PRD Compliance: "Role possède un ensemble de permissions"
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Role name (e.g., ROLE_ADMIN, ROLE_MANAGER, ROLE_LIVREUR, ROLE_CLIENT)
     * Must be unique across the system
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Human-readable description of the role
     */
    @Column(length = 255)
    private String description;

    /**
     * Permissions assigned to this role
     * All users with this role inherit these permissions
     * PRD: "Role possède un ensemble de permissions"
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
