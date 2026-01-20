package com.logismart.security.service;

import com.logismart.security.dto.RegisterRequest;
import com.logismart.security.entity.AuthProvider;
import com.logismart.security.entity.Role;
import com.logismart.security.entity.User;
import com.logismart.security.event.ClientUserRegisteredEvent;
import com.logismart.security.repository.RoleRepository;
import com.logismart.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Register a new user with CLIENT role
     * @param request registration data
     * @return created user
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Get CLIENT role (default role for new registrations)
        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> {
                    log.error("ROLE_CLIENT not found in database");
                    return new IllegalStateException("Default role ROLE_CLIENT not found");
                });

        // Build and save user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .provider(AuthProvider.LOCAL)
                .role(clientRole)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully with role {}", savedUser.getUsername(), clientRole.getName());

        // Publish event to create SenderClient in logismart-api module
        eventPublisher.publishEvent(new ClientUserRegisteredEvent(this, savedUser));
        log.info("Published ClientUserRegisteredEvent for user {}", savedUser.getId());

        return savedUser;
    }

    /**
     * Check if username exists
     * @param username username to check
     * @return true if exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     * @param email email to check
     * @return true if exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
