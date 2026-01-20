package com.logismart.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logismart.security.entity.AuthProvider;
import com.logismart.security.entity.User;
import com.logismart.security.repository.UserRepository;
import com.logismart.security.service.CustomOAuth2User;
import com.logismart.security.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Authentication Success Handler
 * Generates JWT token after successful OAuth2 authentication
 * Returns JWT token to the client (can redirect to frontend or return JSON)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${app.oauth2.frontend-redirect-url:http://localhost:4200/oauth2/redirect}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {

        User user;
        Object principal = authentication.getPrincipal();

        // Handle both CustomOAuth2User (Google, Facebook) and OidcUser (Auth0, Okta)
        if (principal instanceof CustomOAuth2User) {
            // For regular OAuth2 providers (Google, Facebook)
            CustomOAuth2User oAuth2User = (CustomOAuth2User) principal;
            user = oAuth2User.getUser();
        } else if (principal instanceof OidcUser) {
            // For OIDC providers (Auth0, Okta)
            OidcUser oidcUser = (OidcUser) principal;
            String email = oidcUser.getEmail();
            String sub = oidcUser.getSubject();

            // Find user by email or provider ID
            user = userRepository.findByEmail(email)
                    .or(() -> userRepository.findByProviderAndProviderId(AuthProvider.OKTA, sub))
                    .orElseThrow(() -> new RuntimeException("User not found after OIDC authentication"));

            log.info("OIDC authentication successful for user: {}", email);
        } else {
            throw new IllegalStateException("Unknown OAuth2 principal type: " + principal.getClass().getName());
        }

        log.info("OAuth2 authentication successful for user: {}", user.getEmail());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        log.info("Generated JWT token for OAuth2 user: {}", user.getEmail());

        // Option 1: Redirect to frontend with token as URL parameter
        // This is useful for web applications
        String redirectUrl = frontendRedirectUrl + "?token=" + jwtToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        // Option 2: Return JSON response (uncomment if you prefer JSON response)
        // returnJsonResponse(response, user, jwtToken);
    }

    /**
     * Alternative: Return JSON response instead of redirect
     * Useful for mobile apps or SPAs that handle redirects differently
     */
    private void returnJsonResponse(HttpServletResponse response, User user, String jwtToken) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", jwtToken);
        responseBody.put("username", user.getUsername());
        responseBody.put("email", user.getEmail());
        responseBody.put("role", user.getRole().getName());
        responseBody.put("provider", user.getProvider().toString());

        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}