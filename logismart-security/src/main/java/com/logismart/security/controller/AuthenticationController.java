package com.logismart.security.controller;

import com.logismart.security.dto.LoginRequest;
import com.logismart.security.dto.LoginResponse;
import com.logismart.security.dto.RegisterRequest;
import com.logismart.security.entity.User;
import com.logismart.security.service.CustomUserDetailsService;
import com.logismart.security.service.JwtService;
import com.logismart.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final UserService userService;

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

    /**
     * Register a new user account
     * @param registerRequest registration data
     * @return JWT token and user info
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account with CLIENT role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for username: {}", registerRequest.getUsername());

        try {
            // Register the user
            User user = userService.registerUser(registerRequest);

            // Auto-login: Generate JWT token for the newly registered user
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwtToken = jwtService.generateToken(userDetails);

            // Build response
            LoginResponse response = LoginResponse.builder()
                    .token(jwtToken)
                    .role(user.getRole().getName())
                    .username(user.getUsername())
                    .build();

            log.info("User {} registered and logged in successfully", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Registration failed due to system error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }
}