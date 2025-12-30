package com.logismart.security.service;

import com.logismart.security.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Custom OAuth2User implementation that wraps our User entity
 * This allows us to use our User entity throughout the application
 * while still being compatible with Spring Security OAuth2
 */
@RequiredArgsConstructor
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    /**
     * Get the wrapped User entity
     */
    public User getUser() {
        return user;
    }
}