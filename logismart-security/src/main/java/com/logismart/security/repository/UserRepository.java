package com.logismart.security.repository;

import com.logismart.security.entity.AuthProvider;
import com.logismart.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find a user by username
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists by username
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Find a user by email
     * Used for OAuth2 authentication where email is the primary identifier
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by OAuth2 provider and provider ID
     * Used to identify existing OAuth2 users
     * @param provider the authentication provider (GOOGLE, FACEBOOK, etc.)
     * @param providerId the provider-specific user ID
     * @return Optional containing the user if found
     */
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    /**
     * Check if a user exists by email
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
}
