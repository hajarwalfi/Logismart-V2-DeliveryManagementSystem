# 🔐 Spring Security Deep Dive Study Guide
## Complete Reading Order for Exam Preparation

> **Goal**: Understand every line of Spring Security implementation in this project
> **Time Required**: 8-10 hours of focused study
> **Exam Focus**: JWT, Stateless Authentication, Role-Based Authorization, Filters

---

## 📚 Reading Order (Follow Exactly in This Sequence)

### **PHASE 1: Foundation - Understand the Data Model** (1.5 hours)

#### 1. `logismart-security/src/main/java/com/logismart/security/entity/User.java`
**What to understand:**
- [ ] `@Entity` - This is a JPA entity, mapped to `users` table in DB
- [ ] `implements UserDetails` - This makes Spring Security recognize it as an authentication principal
- [ ] **KEY METHOD**: `getAuthorities()` - Returns roles + permissions as `GrantedAuthority` objects
- [ ] Why `@ManyToMany` with Role? One user can have multiple roles
- [ ] Why `@OneToOne` with DeliveryPerson/SenderClient? Links security user to business entities
- [ ] Fields: `username`, `password` (hashed), `email`, `enabled` (account active/disabled)

**Critical lines to know:**
```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    // This returns ROLE_MANAGER, ROLE_LIVREUR, etc. as authorities
    // Spring Security uses this for @PreAuthorize("hasRole('MANAGER')")
}
```

**Questions you should be able to answer:**
- Q: Why does User implement UserDetails?
- Q: What's the difference between `username` and `email`?
- Q: How does Spring Security know if an account is enabled?

---

#### 2. `logismart-security/src/main/java/com/logismart/security/entity/Role.java`
**What to understand:**
- [ ] `@Enumerated(EnumType.STRING)` - Stores role name as string in DB (not integer)
- [ ] `@ManyToMany` with Permission - One role can have many permissions
- [ ] Why we use `Set<Permission>` instead of `List<Permission>` (no duplicates)
- [ ] `@JoinTable` - Creates `role_permissions` junction table for Many-to-Many relationship

**Critical lines to know:**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id")
)
private Set<Permission> permissions = new HashSet<>();
```

**Questions you should be able to answer:**
- Q: What's the difference between EAGER and LAZY fetch?
- Q: Why do we need a separate Permission entity instead of hardcoding permissions?

---

#### 3. `logismart-security/src/main/java/com/logismart/security/entity/Permission.java`
**What to understand:**
- [ ] Structure: `name` (PARCEL_CREATE), `description`, `enabled` flag
- [ ] Why this exists: Dynamic permission management (admin can create new permissions without code changes)
- [ ] Relationship with Role (Many-to-Many)

**Questions you should be able to answer:**
- Q: How is this different from Role?
- Q: How does an admin assign a permission to a role?

---

### **PHASE 2: Configuration - How Spring Security is Set Up** (2 hours)

#### 4. `logismart-security/src/main/java/com/logismart/security/config/SecurityConfig.java`
**THIS IS THE MOST IMPORTANT FILE - READ IT 3 TIMES**

**What to understand LINE BY LINE:**

```java
@Configuration
@EnableWebSecurity  // ← Enables Spring Security
@EnableMethodSecurity  // ← Enables @PreAuthorize on methods
public class SecurityConfig {
```

**Study each @Bean method:**

##### a) **`securityFilterChain(HttpSecurity http)`** - THE CORE CONFIG
```java
http
    .csrf(csrf -> csrf.disable())  // ← Why? Because we use JWT (stateless), not cookies
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
        // ↑ These endpoints are PUBLIC (no authentication needed)
        .anyRequest().authenticated()
        // ↑ ALL other endpoints require authentication
    )
    .sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // ↑ CRITICAL: No server-side sessions (no JSESSIONID cookie)
    .authenticationProvider(authenticationProvider())
        // ↑ Tells Spring how to validate credentials
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // ↑ Adds our custom JWT filter BEFORE the default authentication filter
```

**MEMORIZE THIS ORDER:**
1. Disable CSRF (we're stateless)
2. Define which URLs are public vs authenticated
3. Set session policy to STATELESS (this is what makes it JWT-based)
4. Add our JWT filter to the chain

**Questions you should be able to answer:**
- Q: Why disable CSRF for JWT?
- Q: What does STATELESS mean?
- Q: What happens if we remove `addFilterBefore()`?
- Q: Why is `/auth/login` public but `/api/parcels` is authenticated?

##### b) **`authenticationProvider()`**
```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    // ↑ Load user from database
    authProvider.setPasswordEncoder(passwordEncoder());
    // ↑ Validate password using BCrypt
    return authProvider;
}
```

**Questions you should be able to answer:**
- Q: What is DaoAuthenticationProvider?
- Q: Why do we need a UserDetailsService?

##### c) **`passwordEncoder()`**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // ← One-way hash, can't be reversed
}
```

**Questions you should be able to answer:**
- Q: Why BCrypt instead of MD5 or SHA-256?
- Q: Can we decrypt a BCrypt password? (NO! It's a one-way hash)

##### d) **`authenticationManager()`**
```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

**Questions you should be able to answer:**
- Q: What does AuthenticationManager do?
- Q: When is it used? (During login to validate username/password)

---

#### 5. `logismart-security/src/main/java/com/logismart/security/config/CorsConfig.java`
**What to understand:**
- [ ] CORS = Cross-Origin Resource Sharing (allows frontend on different domain to call API)
- [ ] `allowedOrigins`: localhost:3000 (React), localhost:4200 (Angular)
- [ ] `allowedMethods`: GET, POST, PUT, DELETE, etc.
- [ ] `allowedHeaders`: Authorization (for JWT token), Content-Type
- [ ] `allowCredentials(true)`: Allows cookies (though we don't use them for JWT)

**Questions you should be able to answer:**
- Q: What happens if we don't configure CORS?
- Q: Why do we need to allow the Authorization header?

---

### **PHASE 3: Authentication Flow - Login & Token Generation** (2 hours)

#### 6. `logismart-security/src/main/java/com/logismart/security/dto/LoginRequest.java`
**Simple DTO - just know it has:**
- `username` (String)
- `password` (String)

#### 7. `logismart-security/src/main/java/com/logismart/security/dto/LoginResponse.java`
**What gets returned after successful login:**
- `token` (the JWT string)
- `type` ("Bearer")
- `username`
- `role` (ROLE_MANAGER, etc.)

---

#### 8. `logismart-security/src/main/java/com/logismart/security/service/CustomUserDetailsService.java`
**CRITICAL SERVICE - Understand every line**

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user;  // ← User implements UserDetails, so we can return it directly
    }
}
```

**Questions you should be able to answer:**
- Q: When is this method called? (During login AND during JWT validation)
- Q: What happens if username is not found? (Throws UsernameNotFoundException)
- Q: Why does it return UserDetails instead of User? (Spring Security interface requirement)

---

#### 9. `logismart-security/src/main/java/com/logismart/security/service/JwtService.java`
**THE MOST COMPLEX FILE - Study for 1 hour minimum**

**Study each method in this order:**

##### a) **`generateToken(UserDetails userDetails)`**
```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> extraClaims = new HashMap<>();
    // Add authorities (roles + permissions) to JWT
    extraClaims.put("authorities", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));

    return buildToken(extraClaims, userDetails, jwtExpiration);
}
```

**What happens:**
1. Creates a map to hold extra data (claims)
2. Adds user's authorities (roles/permissions) to the token
3. Calls `buildToken()` to create the actual JWT string

##### b) **`buildToken()` - Creates the actual JWT**
```java
private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .claims(extraClaims)  // ← Add custom data (authorities)
        .subject(userDetails.getUsername())  // ← "sub" claim (who the token is for)
        .issuedAt(new Date(System.currentTimeMillis()))  // ← "iat" claim (when issued)
        .expiration(new Date(System.currentTimeMillis() + expiration))  // ← "exp" claim (24 hours)
        .signWith(getSigningKey())  // ← Sign with secret key (prevents tampering)
        .compact();  // ← Build the final JWT string
}
```

**The JWT token structure:**
```
eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtYW5hZ2VyIiwiaWF0IjoxNzM0NTQ...
│                     │                                            │
Header (algorithm)    Payload (claims: sub, iat, exp, authorities) Signature
```

##### c) **`getSigningKey()`**
```java
private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);  // ← secretKey from application.yml
    return Keys.hmacShaKeyFor(keyBytes);
}
```

**What to know:**
- Secret key is stored in `application.yml` (`jwt.secret`)
- Must be at least 256 bits for HMAC-SHA256
- This key is used to SIGN the token (so we can verify it wasn't tampered with)

##### d) **`extractUsername(String token)` - Parse token to get username**
```java
public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);  // ← Get "sub" claim
}
```

##### e) **`extractAllClaims(String token)` - Parse and verify token**
```java
private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())  // ← CRITICAL: Verify signature (detect tampering)
        .build()
        .parseSignedClaims(token)
        .getPayload();  // ← Return the claims (sub, iat, exp, authorities)
}
```

**What happens:**
1. Uses the secret key to verify the signature
2. If signature is invalid → throws exception
3. If signature is valid → extracts the claims

##### f) **`isTokenValid(String token, UserDetails userDetails)`**
```java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}
```

**Checks:**
1. Username in token matches the loaded user
2. Token hasn't expired

##### g) **`isTokenExpired(String token)`**
```java
private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
```

**Questions you should be able to answer:**
- Q: What algorithm is used to sign the JWT? (HMAC-SHA384, see `@Value("${jwt.secret}")`)
- Q: What claims are in the token? (sub, iat, exp, authorities)
- Q: Can someone modify the token and still use it? (NO - signature verification will fail)
- Q: What happens if the token expires?
- Q: Where is the secret key stored? (application.yml)

---

#### 10. `logismart-security/src/main/java/com/logismart/security/controller/AuthenticationController.java`
**The Login Endpoint**

```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    // 1. Authenticate username/password
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        )
    );

    // 2. If successful, get the user
    User user = (User) authentication.getPrincipal();

    // 3. Generate JWT token
    String jwtToken = jwtService.generateToken(user);

    // 4. Return token to client
    return ResponseEntity.ok(LoginResponse.builder()
        .token(jwtToken)
        .type("Bearer")
        .username(user.getUsername())
        .role(user.getRoles().iterator().next().name())
        .build());
}
```

**TRACE THE FLOW:**
1. Client sends POST /auth/login with {username, password}
2. AuthenticationManager validates credentials
   - Uses CustomUserDetailsService to load user from DB
   - Uses BCryptPasswordEncoder to verify password
3. If valid, creates Authentication object
4. JwtService generates token with user's roles/permissions
5. Returns token to client
6. Client stores token and sends it in Authorization header for future requests

**Questions you should be able to answer:**
- Q: What happens if password is wrong? (AuthenticationManager throws BadCredentialsException → 401)
- Q: What is UsernamePasswordAuthenticationToken?
- Q: Why do we need to cast authentication.getPrincipal() to User?

---

### **PHASE 4: Authorization Flow - Validating Requests** (2.5 hours)

#### 11. `logismart-security/src/main/java/com/logismart/security/filter/JwtAuthenticationFilter.java`
**THE MOST CRITICAL FILE FOR UNDERSTANDING AUTHORIZATION**

**Read this filter METHOD BY METHOD:**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // STEP 1: Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // STEP 2: Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // ← Continue to next filter (will fail at authorization)
            return;
        }

        // STEP 3: Extract the token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);  // ← Parse JWT to get username

        // STEP 4: If username exists and user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // STEP 5: Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // STEP 6: Validate the token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // STEP 7: Create authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()  // ← CRITICAL: Roles + permissions
                );

                authToken.setDetails(new WebAuthenticationDetailsImpl(request));

                // STEP 8: Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
                // ↑ NOW Spring Security knows who the user is and what roles/permissions they have
            }
        }

        // STEP 9: Continue to next filter
        filterChain.doFilter(request, response);
    }
}
```

**MEMORIZE THIS FLOW:**
```
Request comes in with Authorization: Bearer eyJhbGci...
    ↓
Extract token from header
    ↓
Parse token to get username
    ↓
Load user from database (with roles/permissions)
    ↓
Validate token (signature + expiration)
    ↓
Create Authentication object with authorities
    ↓
Store in SecurityContextHolder
    ↓
Continue to controller (now @PreAuthorize can check roles)
```

**Questions you should be able to answer:**
- Q: Why does this extend OncePerRequestFilter? (Ensures filter runs once per request)
- Q: What happens if token is invalid? (Authentication is not set in SecurityContext → 401)
- Q: What is SecurityContextHolder? (Thread-local storage for current authentication)
- Q: When does this filter run? (On EVERY request, before the controller)
- Q: Why do we load user from DB again? (To get latest roles/permissions in case they changed)

---

### **PHASE 5: Role-Based Authorization in Controllers** (1.5 hours)

#### 12. `logismart-api/src/main/java/com/logismart/logismartv2/controller/ParcelController.java`
**Example controller with role-based authorization**

**Study these annotations:**

```java
@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    // MANAGER can see all parcels
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<Page<ParcelResponseDTO>> getAllParcels() {
        // ...
    }

    // MANAGER can create parcels
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ParcelResponseDTO> createParcel(@RequestBody ParcelCreateDTO dto) {
        // ...
    }

    // LIVREUR can see only their assigned parcels
    @PreAuthorize("hasAnyRole('LIVREUR', 'CLIENT')")
    @GetMapping("/my-parcels")
    public ResponseEntity<List<ParcelResponseDTO>> getMyParcels() {
        // Uses SecurityContextHolder to get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // ...
    }

    // LIVREUR can update status of their parcels
    @PreAuthorize("hasRole('LIVREUR')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ParcelResponseDTO> updateParcelStatus(
        @PathVariable UUID id,
        @RequestParam ParcelStatus status
    ) {
        // ...
    }
}
```

**How @PreAuthorize works:**
1. Request comes in with JWT token
2. JwtAuthenticationFilter sets authentication in SecurityContext (with roles)
3. Spring Security checks @PreAuthorize BEFORE entering the method
4. If user has the required role → method executes
5. If user doesn't have the role → throws AccessDeniedException → 403 Forbidden

**Questions you should be able to answer:**
- Q: What's the difference between `hasRole('MANAGER')` and `hasAuthority('MANAGER')`?
  - hasRole() adds "ROLE_" prefix automatically
  - hasAuthority() uses exact string
- Q: What happens if a LIVREUR tries to call GET /api/parcels? (403 Forbidden)
- Q: How does the controller know who the current user is? (SecurityContextHolder.getContext().getAuthentication())

---

#### 13. Study these controllers for different role patterns:
- `ZoneController.java` - MANAGER only
- `DeliveryPersonController.java` - MANAGER only
- `DeliveryHistoryController.java` - MANAGER can see all, LIVREUR sees only their history
- `StatisticsController.java` - MANAGER only

---

### **PHASE 6: Dynamic Permissions System (Advanced)** (1.5 hours)

#### 14. `logismart-security/src/main/java/com/logismart/security/controller/PermissionController.java`
**ADMIN endpoints to manage permissions**

```java
@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN')")  // ← Only ADMIN can access
public class PermissionController {

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(@RequestBody PermissionRequest request) {
        // Create new permission (e.g., "REPORT_EXPORT")
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        // List all permissions
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        // Delete permission
    }
}
```

**Questions you should be able to answer:**
- Q: Why do we need dynamic permissions?
- Q: What's the difference between Role and Permission?
- Q: Can a MANAGER create permissions? (NO, only ADMIN)

---

#### 15. `logismart-security/src/main/java/com/logismart/security/service/PermissionService.java`
**Business logic for permission management**

Study the CRUD operations for permissions.

---

### **PHASE 7: Database & Repository Layer** (1 hour)

#### 16. `logismart-security/src/main/java/com/logismart/security/repository/UserRepository.java`
```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}
```

**Know:**
- Extends JpaRepository (gives CRUD methods for free)
- Custom query methods (Spring Data JPA generates SQL automatically)

---

#### 17. Database Migrations
**File:** `logismart-api/src/main/resources/db/changelog/changes/005-create-permissions-system.xml`

**Understand:**
- Liquibase creates tables: `users`, `roles`, `permissions`, `role_permissions`
- Seeds initial data (admin user, default permissions)

---

### **PHASE 8: Testing & Configuration** (30 min)

#### 18. `logismart-api/src/main/resources/application.yml`
**Configuration values:**
```yaml
jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration: 86400000  # 24 hours in milliseconds

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/Logismart-V2
    username: postgres
    password: BLISSFUL
```

---

## 🎯 Exam Question Checklist

### **Authentication Questions:**
- [ ] Explain the complete login flow (POST /auth/login → token generation)
- [ ] What is JWT and how is it structured? (Header.Payload.Signature)
- [ ] How do you generate a JWT? (JwtService.generateToken())
- [ ] What claims are in the JWT? (sub, iat, exp, authorities)
- [ ] How is the JWT signed? (HMAC-SHA with secret key)
- [ ] Why is the secret key important?
- [ ] What is BCrypt and why use it?

### **Authorization Questions:**
- [ ] Explain the request validation flow (JwtAuthenticationFilter)
- [ ] How does Spring Security know the current user's roles?
- [ ] What is SecurityContextHolder?
- [ ] How does @PreAuthorize work?
- [ ] What's the difference between hasRole() and hasAuthority()?
- [ ] What happens if a user tries to access an endpoint without permission? (403)

### **Architecture Questions:**
- [ ] Why is session policy set to STATELESS?
- [ ] What is the filter chain and where does JwtAuthenticationFilter fit?
- [ ] Why disable CSRF for JWT?
- [ ] What is UserDetailsService and when is it called?
- [ ] Explain the multi-module architecture (security vs api)

### **Code-Level Questions:**
- [ ] Walk through SecurityConfig line by line
- [ ] Walk through JwtAuthenticationFilter line by line
- [ ] Explain how roles and permissions are stored in the database
- [ ] How do you add a new role?
- [ ] How do you secure a new endpoint?

---

## 📖 Final Study Strategy

### **Day 1: Read & Understand**
- Phase 1-3 (6 hours)
- Take notes on each file
- Draw diagrams of the flows

### **Day 2: Deep Dive & Memorize**
- Phase 4-6 (5 hours)
- Trace code execution with debugger
- Practice explaining out loud

### **Day 3: Review & Test**
- Phase 7-8 (2 hours)
- Answer all checklist questions
- Simulate exam questions

---

## 🔥 Pro Tips for Exam

1. **Draw the flows on paper** - Authentication flow, Authorization flow
2. **Memorize key annotations** - @PreAuthorize, @EnableMethodSecurity, @Component
3. **Know the filter order** - JwtAuthenticationFilter runs BEFORE controllers
4. **Understand the difference** - Authentication (who are you?) vs Authorization (what can you do?)
5. **Know error codes** - 401 (unauthenticated), 403 (unauthorized/forbidden)

---

Good luck! 🚀
