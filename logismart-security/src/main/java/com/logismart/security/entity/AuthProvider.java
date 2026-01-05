package com.logismart.security.entity;

/**
 * Authentication provider types
 * Defines the source of user authentication
 */
public enum AuthProvider {
    /**
     * Local authentication (email/password)
     */
    LOCAL,

    /**
     * Google OAuth2 authentication
     */
    GOOGLE,

    /**
     * Facebook OAuth2 authentication
     */
    FACEBOOK,

    /**
     * Apple OAuth2 authentication
     */
    APPLE,

    /**
     * Okta identity provider
     */
    OKTA
}