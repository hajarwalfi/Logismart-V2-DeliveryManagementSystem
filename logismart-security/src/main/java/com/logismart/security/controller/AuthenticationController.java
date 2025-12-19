package com.logismart.security.controller;

import com.logismart.security.dto.LoginRequest;
import com.logismart.security.dto.LoginResponse;
import com.logismart.security.entity.User;
import com.logismart.security.service.CustomUserDetailsService;
import com.logismart.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Authenticate user and return JWT token
     * @param loginRequest login credentials
     * @return JWT token and user role
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticate user with username and password, returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(userDetails);

        // Get user role
        String role = ((User) userDetails).getRole().getName();

        log.info("User {} authenticated successfully with role {}", loginRequest.getUsername(), role);

        // Build response
        LoginResponse response = LoginResponse.builder()
                .token(jwtToken)
                .role(role)
                .username(userDetails.getUsername())
                .build();

        return ResponseEntity.ok(response);
    }
}