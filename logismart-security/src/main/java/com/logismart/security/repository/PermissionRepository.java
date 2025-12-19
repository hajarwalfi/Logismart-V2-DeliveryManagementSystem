package com.logismart.security.repository;

import com.logismart.security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    /**
     * Find permission by name
     */
    Optional<Permission> findByName(String name);

    /**
     * Check if permission exists by name
     */
    boolean existsByName(String name);

    /**
     * Find all permissions for a specific resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find all permissions for a specific action
     */
    List<Permission> findByAction(String action);

    /**
     * Find all enabled permissions
     */
    List<Permission> findByEnabledTrue();

    /**
     * Find permissions by resource and action
     */
    List<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Search permissions by name or description
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Permission> searchPermissions(@Param("keyword") String keyword);
}
