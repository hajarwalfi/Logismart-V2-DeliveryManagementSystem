package com.logismart.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission entity for fine-grained access control
 * Allows dynamic management of permissions without code changes
 */
@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Unique permission name (e.g., "PARCEL_CREATE", "ZONE_READ", "STATS_VIEW")
     * Used in @PreAuthorize("hasAuthority('PARCEL_CREATE')")
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Human-readable description of what this permission allows
     */
    @Column(length = 255)
    private String description;

    /**
     * Resource category (e.g., "PARCEL", "ZONE", "DELIVERY", "STATISTICS")
     * Helps organize permissions by domain
     */
    @Column(length = 50)
    private String resource;

    /**
     * Action type (e.g., "CREATE", "READ", "UPDATE", "DELETE", "VIEW")
     * Represents what operation this permission grants
     */
    @Column(length = 20)
    private String action;

    /**
     * Whether this permission is active
     * Allows disabling permissions without deleting them
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
}
