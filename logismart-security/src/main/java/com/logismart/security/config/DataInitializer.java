package com.logismart.security.config;

import com.logismart.security.entity.Role;
import com.logismart.security.entity.User;
import com.logismart.security.repository.RoleRepository;
import com.logismart.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes default users in the database on application startup
 * Creates an admin user and a delivery person (livreur) if they don't exist
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data initialization...");

        // Initialize roles first
        initializeRoles();

        // Initialize users
        initializeUsers();

        log.info("Data initialization completed successfully");
    }

    private void initializeRoles() {
        // Create ROLE_MANAGER (Admin/Gestionnaire) if it doesn't exist
        // PRD: Full access - manage parcels, delivery persons, zones, statistics
        if (!roleRepository.existsByName("ROLE_MANAGER")) {
            Role managerRole = Role.builder()
                    .name("ROLE_MANAGER")
                    .description("Manager/Admin with full system access - manage parcels, delivery persons, zones, statistics")
                    .build();
            // ID will be auto-generated as UUID by database
            roleRepository.save(managerRole);
            log.info("Created role: ROLE_MANAGER with UUID: {}", managerRole.getId());
        }

        // Create ROLE_LIVREUR if it doesn't exist
        // PRD: View assigned parcels only, update status (collected, in transit, delivered)
        // No access to other delivery persons' data or sensitive information
        if (!roleRepository.existsByName("ROLE_LIVREUR")) {
            Role livreurRole = Role.builder()
                    .name("ROLE_LIVREUR")
                    .description("Delivery person - view assigned parcels, update delivery status")
                    .build();
            // ID will be auto-generated as UUID by database
            roleRepository.save(livreurRole);
            log.info("Created role: ROLE_LIVREUR with UUID: {}", livreurRole.getId());
        }

        // Create ROLE_CLIENT if it doesn't exist
        // PRD: Create delivery requests, view own parcels only, receive tracking updates
        // No access to delivery persons, zones, routes
        if (!roleRepository.existsByName("ROLE_CLIENT")) {
            Role clientRole = Role.builder()
                    .name("ROLE_CLIENT")
                    .description("Sender client - create delivery requests, view own parcels, receive tracking")
                    .build();
            // ID will be auto-generated as UUID by database
            roleRepository.save(clientRole);
            log.info("Created role: ROLE_CLIENT with UUID: {}", clientRole.getId());
        }
    }

    private void initializeUsers() {
        // Create admin/manager user
        if (!userRepository.existsByUsername("admin")) {
            Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                    .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found"));

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(managerRole)
                    .build();
            // ID will be auto-generated as UUID by database
            userRepository.save(admin);
            log.info("Created default admin user (username: admin) with UUID: {}", admin.getId());
        } else {
            log.info("Admin user already exists, skipping creation");
        }

        // Create livreur user
        if (!userRepository.existsByUsername("livreur")) {
            Role livreurRole = roleRepository.findByName("ROLE_LIVREUR")
                    .orElseThrow(() -> new RuntimeException("ROLE_LIVREUR not found"));

            User livreur = User.builder()
                    .username("livreur")
                    .password(passwordEncoder.encode("livreur123"))
                    .role(livreurRole)
                    .build();
            // ID will be auto-generated as UUID by database
            userRepository.save(livreur);
            log.info("Created default livreur user (username: livreur) with UUID: {}", livreur.getId());
        } else {
            log.info("Livreur user already exists, skipping creation");
        }
    }
}