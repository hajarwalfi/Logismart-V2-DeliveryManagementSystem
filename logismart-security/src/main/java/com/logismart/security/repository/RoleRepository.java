package com.logismart.security.repository;

import com.logismart.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity
 * PRD: Manages roles that contain permissions
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    /**
     * Find role by name (e.g., "ROLE_ADMIN", "ROLE_MANAGER")
     */
    Optional<Role> findByName(String name);

    /**
     * Check if role exists by name
     */
    boolean existsByName(String name);
}
