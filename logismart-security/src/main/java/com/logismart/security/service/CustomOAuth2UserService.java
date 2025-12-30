package com.logismart.security.service;

import com.logismart.security.entity.AuthProvider;
import com.logismart.security.entity.Role;
import com.logismart.security.entity.User;
import com.logismart.security.repository.RoleRepository;
import com.logismart.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Custom OAuth2 User Service
 * Handles user creation and update after OAuth2 authentication
 * Extracts user info from OAuth2 provider and creates/updates user in database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Get OAuth2 user from provider
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Get provider name (google, facebook, etc.)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = getProviderFromRegistrationId(registrationId);

        // Extract user info from OAuth2 provider
        String providerId = extractProviderId(registrationId, oAuth2User);
        String email = extractEmail(registrationId, oAuth2User);
        String firstName = extractFirstName(registrationId, oAuth2User);
        String lastName = extractLastName(registrationId, oAuth2User);

        log.info("OAuth2 authentication - Provider: {}, Email: {}, ProviderId: {}", provider, email, providerId);

        // Create or update user
        User user = processOAuth2User(provider, providerId, email, firstName, lastName, oAuth2User.getAttributes());

        // Return CustomOAuth2User with our User entity
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    /**
     * Create or update user from OAuth2 provider
     */
    private User processOAuth2User(AuthProvider provider, String providerId, String email,
                                   String firstName, String lastName, Map<String, Object> attributes) {

        // Try to find existing user by provider and providerId
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            log.info("Updating existing OAuth2 user: {}", email);
            return userRepository.save(user);
        }

        // Check if user with this email already exists (could be LOCAL user)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            // Link OAuth2 provider to existing account
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            log.info("Linking OAuth2 provider {} to existing user: {}", provider, email);
            return userRepository.save(user);
        }

        // Create new user
        log.info("Creating new OAuth2 user: {}", email);

        // Get default role (ROLE_CLIENT)
        Role defaultRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_CLIENT not found"));

        User newUser = User.builder()
                .username(email) // Use email as username for OAuth2 users
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .provider(provider)
                .providerId(providerId)
                .password(null) // No password for OAuth2 users
                .role(defaultRole)
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Extract provider ID based on OAuth2 provider
     */
    private String extractProviderId(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub"); // Google uses "sub"
            case "facebook" -> oAuth2User.getAttribute("id"); // Facebook uses "id"
            case "apple" -> oAuth2User.getAttribute("sub"); // Apple uses "sub"
            case "okta" -> oAuth2User.getAttribute("sub"); // Okta uses "sub"
            default -> oAuth2User.getAttribute("sub");
        };
    }

    /**
     * Extract email based on OAuth2 provider
     */
    private String extractEmail(String registrationId, OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("email");
    }

    /**
     * Extract first name based on OAuth2 provider
     */
    private String extractFirstName(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("given_name");
            case "facebook" -> {
                String name = oAuth2User.getAttribute("name");
                yield name != null && name.contains(" ") ? name.split(" ")[0] : name;
            }
            case "apple" -> {
                // Apple sends name in a nested object on first login only
                Map<String, Object> name = oAuth2User.getAttribute("name");
                if (name != null) {
                    yield (String) name.get("firstName");
                }
                yield null;
            }
            case "okta" -> oAuth2User.getAttribute("given_name"); // Okta uses "given_name"
            default -> oAuth2User.getAttribute("given_name");
        };
    }

    /**
     * Extract last name based on OAuth2 provider
     */
    private String extractLastName(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("family_name");
            case "facebook" -> {
                String name = oAuth2User.getAttribute("name");
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ");
                    yield parts.length > 1 ? parts[parts.length - 1] : null;
                }
                yield null;
            }
            case "apple" -> {
                // Apple sends name in a nested object on first login only
                Map<String, Object> name = oAuth2User.getAttribute("name");
                if (name != null) {
                    yield (String) name.get("lastName");
                }
                yield null;
            }
            case "okta" -> oAuth2User.getAttribute("family_name"); // Okta uses "family_name"
            default -> oAuth2User.getAttribute("family_name");
        };
    }

    /**
     * Convert registration ID to AuthProvider enum
     */
    private AuthProvider getProviderFromRegistrationId(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "facebook" -> AuthProvider.FACEBOOK;
            case "apple" -> AuthProvider.APPLE;
            case "okta" -> AuthProvider.OKTA;
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
}