package com.logismart.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    /**
     * Password - nullable for OAuth2 users who don't have passwords
     * Required for LOCAL authentication, null for OAuth2 providers
     */
    @Column(nullable = true)
    private String password;

    /**
     * Authentication provider (LOCAL, GOOGLE, FACEBOOK, APPLE, OKTA)
     * Indicates how the user authenticated
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    /**
     * Provider-specific user ID
     * Stores the unique identifier from OAuth2 provider (e.g., Google user ID)
     * Null for LOCAL authentication
     */
    private String providerId;

    /**
     * User's role in the system
     * User inherits all permissions from this role
     * PRD: User gets permissions via Role
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Get authorities from role's permissions
     * PRD Compliance: "Role possède un ensemble de permissions"
     * User inherits permissions from their role
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role name as authority (for @PreAuthorize("hasRole('...')"))
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            // Add all permissions from the role
            if (role.getPermissions() != null) {
                authorities.addAll(role.getPermissions().stream()
                        .filter(Permission::getEnabled)
                        .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                        .collect(Collectors.toSet()));
            }
        }

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
