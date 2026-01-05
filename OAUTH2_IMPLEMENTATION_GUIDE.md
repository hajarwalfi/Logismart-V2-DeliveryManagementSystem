# OAuth2 Implementation Guide - SmartLogi Delivery System

## Overview

This document explains the hybrid JWT + OAuth2 authentication implementation in SmartLogi Delivery Management System.

## Architecture

The system now supports **TWO** authentication methods:
1. **Traditional (LOCAL)**: Username/Password → JWT Token
2. **OAuth2 Social Login**: Google/Facebook → JWT Token

**Key Point**: Regardless of authentication method, users receive the **same JWT token format** and access APIs the **same way**.

---

## Authentication Flows

### Flow 1: Traditional Login (Username/Password)

```
User → POST /auth/login (username + password)
      → AuthenticationController validates credentials
      → JwtService generates JWT token
      → Response: { token, role, username }

User → Accesses /api/** with "Authorization: Bearer {JWT}"
```

### Flow 2: OAuth2 Login (Google/Facebook)

```
User → Clicks "Login with Google" in frontend
      → Frontend redirects to: http://localhost:8080/oauth2/authorization/google

Spring Security → Redirects to Google OAuth2
User → Authorizes on Google
Google → Redirects back: http://localhost:8080/login/oauth2/code/google?code=...

Spring Security → Exchanges code for access token
                → Calls Google user info endpoint
CustomOAuth2UserService → Receives user data (email, name, etc.)
                        → Checks if user exists (by provider + providerId)
                        → Creates new user OR updates existing user
                        → Assigns default role: CLIENT

OAuth2AuthenticationSuccessHandler → Generates JWT token (same as traditional login)
                                   → Redirects to frontend:
                                     http://localhost:4200/oauth2/redirect?token={JWT}

Frontend → Extracts token from URL
         → Stores token in localStorage
         → Accesses /api/** with "Authorization: Bearer {JWT}"
```

---

## Database Schema Changes

### New Fields in `users` Table

| Field | Type | Description |
|-------|------|-------------|
| `email` | VARCHAR(255) | User's email address (unique) |
| `first_name` | VARCHAR(100) | First name from OAuth2 provider |
| `last_name` | VARCHAR(100) | Last name from OAuth2 provider |
| `provider` | VARCHAR(20) | Authentication provider: LOCAL, GOOGLE, FACEBOOK, APPLE, OKTA |
| `provider_id` | VARCHAR(255) | Unique ID from OAuth2 provider |
| `password` | VARCHAR(255) | **NOW NULLABLE** (OAuth2 users don't have passwords) |

### Indexes

- `idx_users_email` - Fast email lookups
- `idx_users_provider_providerid` - Fast OAuth2 user identification

---

## Configuration

### 1. Environment Variables (Required for Production)

Create a `.env` file or set system environment variables:

```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id-from-console
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Facebook OAuth2
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-app-secret

# Frontend URLs
FRONTEND_REDIRECT_URL=http://localhost:4200/oauth2/redirect
FRONTEND_ERROR_URL=http://localhost:4200/oauth2/error
```

### 2. How to Get Google OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable "Google+ API"
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
5. Application type: "Web application"
6. Authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://your-domain.com/login/oauth2/code/google` (for production)
7. Copy Client ID and Client Secret

### 3. How to Get Facebook OAuth2 Credentials

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create an app
3. Add "Facebook Login" product
4. Settings → Basic: Copy App ID and App Secret
5. Facebook Login Settings → Valid OAuth Redirect URIs:
   - `http://localhost:8080/login/oauth2/code/facebook`
   - `http://your-domain.com/login/oauth2/code/facebook`

### 4. How to Get Apple Sign In Credentials

**Note**: Apple Sign In setup is more complex and requires generating a client secret JWT.

#### Step 1: Apple Developer Account Setup
1. You need an **Apple Developer Account** ($99/year)
2. Go to [Apple Developer Console](https://developer.apple.com/account/)

#### Step 2: Create App ID
1. Go to **Certificates, Identifiers & Profiles**
2. Click **Identifiers** → **+** (Add new)
3. Select **App IDs** → Click **Continue**
4. Select **App** → Click **Continue**
5. Fill in:
   - **Description**: e.g., "SmartLogi App"
   - **Bundle ID**: e.g., `com.logismart.app` (Explicit)
6. Check **Sign In with Apple** capability
7. Click **Continue** → **Register**

#### Step 3: Create Services ID (OAuth Client)
1. Go to **Identifiers** → **+** (Add new)
2. Select **Services IDs** → Click **Continue**
3. Fill in:
   - **Description**: e.g., "SmartLogi Web Service"
   - **Identifier**: e.g., `com.logismart.service` (This is your APPLE_CLIENT_ID)
4. Click **Continue** → **Register**
5. Click on the newly created Services ID
6. Check **Sign In with Apple**
7. Click **Configure** next to Sign In with Apple
8. **Primary App ID**: Select the App ID created in Step 2
9. **Website URLs**:
   - **Domains and Subdomains**: `localhost` (for development), `yourdomain.com` (for production)
   - **Return URLs**:
     - `http://localhost:8080/login/oauth2/code/apple`
     - `https://yourdomain.com/login/oauth2/code/apple` (for production)
10. Click **Save** → **Continue** → **Register**

#### Step 4: Create Private Key for Sign In with Apple
1. Go to **Keys** → **+** (Add new)
2. **Key Name**: e.g., "SmartLogi Sign In Key"
3. Check **Sign In with Apple**
4. Click **Configure** → Select your **Primary App ID**
5. Click **Save** → **Continue** → **Register**
6. **Download the .p8 key file** (you can only download it once!)
7. Note the **Key ID** (you'll need this)

#### Step 5: Get Your Team ID
1. Go to **Membership** in Apple Developer Console
2. Copy your **Team ID** (10-character string)

#### Step 6: Generate Client Secret JWT

Apple requires a **JWT token** as the client secret, which you must generate programmatically. This JWT:
- Must be signed with your private key (.p8 file)
- Expires after 6 months (you need to regenerate it)
- Uses ES256 algorithm

**You have two options**:

**Option A: Use Online JWT Generator** (easier for testing)
- Use tools like https://jwt.io or create a script

**Option B: Create a Java Utility** (recommended)

Create a utility class in your project to generate the Apple client secret:

```java
// Add this dependency to pom.xml:
// <dependency>
//   <groupId>io.jsonwebtoken</groupId>
//   <artifactId>jjwt-api</artifactId>
// </dependency>

// Then create AppleClientSecretGenerator.java:
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AppleClientSecretGenerator {
    public static String generateClientSecret(
            String teamId,
            String clientId,
            String keyId,
            String privateKeyPath) throws Exception {

        // Read private key
        String privateKeyContent = new String(Files.readAllBytes(new File(privateKeyPath).toPath()))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = java.util.Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey privateKey = kf.generatePrivate(spec);

        // Generate JWT
        Date expirationDate = Date.from(LocalDateTime.now().plusMonths(6)
                .atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    public static void main(String[] args) throws Exception {
        String teamId = "YOUR_TEAM_ID";
        String clientId = "com.logismart.service"; // Your Services ID
        String keyId = "YOUR_KEY_ID";
        String privateKeyPath = "/path/to/AuthKey_XXXXXX.p8";

        String clientSecret = generateClientSecret(teamId, clientId, keyId, privateKeyPath);
        System.out.println("Apple Client Secret: " + clientSecret);
    }
}
```

#### Step 7: Update application.yml

Add the generated client secret to your `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          apple:
            client-id: com.logismart.service  # Your Services ID
            client-secret: eyJraWQiOiI... # Generated JWT from Step 6
            scope:
              - name
              - email
```

**Important Notes**:
- The client secret JWT expires after 6 months - you'll need to regenerate it
- Apple only sends user name on the **first login** - subsequent logins only provide email and sub
- Users can choose to "Hide My Email" - Apple will generate a proxy email

### 5. How to Get Okta OAuth2 Credentials

**Note**: Okta is **FREE** for developers (up to 15,000 monthly active users) - no paid account needed!

#### Step 1: Create Okta Developer Account
1. Go to [Okta Developer](https://developer.okta.com/signup/)
2. Sign up for a **free developer account**
3. Verify your email
4. You'll get your **Okta domain** (e.g., `https://dev-1234567.okta.com`)

#### Step 2: Create an Application
1. Log in to your [Okta Admin Console](https://developer.okta.com/login/)
2. Go to **Applications** → **Applications**
3. Click **Create App Integration**
4. Choose:
   - **Sign-in method**: OIDC - OpenID Connect
   - **Application type**: Web Application
5. Click **Next**

#### Step 3: Configure Application Settings
1. **App integration name**: e.g., "SmartLogi Delivery System"
2. **Grant type**: Check **Authorization Code**
3. **Sign-in redirect URIs**:
   - Add: `http://localhost:8080/login/oauth2/code/okta`
   - For production: `https://yourdomain.com/login/oauth2/code/okta`
4. **Sign-out redirect URIs** (optional):
   - Add: `http://localhost:4200`
5. **Controlled access**: Choose **Allow everyone in your organization to access**
6. Click **Save**

#### Step 4: Get Client Credentials
After creating the app, you'll see:
- **Client ID**: Copy this (e.g., `0oa1b2c3d4e5f6g7h8i9`)
- **Client secret**: Copy this as well

#### Step 5: Configure Authorization Server
1. Go to **Security** → **API** → **Authorization Servers**
2. You'll see a **default** authorization server
3. Copy the **Issuer URI** (e.g., `https://dev-1234567.okta.com/oauth2/default`)
4. This is your `OKTA_ISSUER_URI`

#### Step 6: Update application.yml

Add your Okta credentials to `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          okta:
            client-id: 0oa1b2c3d4e5f6g7h8i9  # Your Client ID
            client-secret: your-client-secret  # Your Client Secret
            scope:
              - openid
              - profile
              - email
        provider:
          okta:
            issuer-uri: https://dev-1234567.okta.com/oauth2/default  # Your Issuer URI
```

**That's it!** Okta configuration is much simpler than Apple since:
- No JWT generation needed
- Standard OAuth2/OIDC implementation
- Spring Boot auto-configures endpoints using the issuer URI

#### Testing Okta Login
1. Restart your Spring Boot application
2. Open browser: `http://localhost:8080/oauth2/authorization/okta`
3. Sign in with your Okta developer account
4. You'll be redirected back with a JWT token

#### Okta Features
- **Free tier**: 15,000 monthly active users
- **User management**: Built-in user directory
- **Multi-factor authentication (MFA)**: Free to enable
- **Social login**: Can configure Okta to use Google, Facebook, etc.
- **Enterprise features**: SAML, LDAP integration (paid plans)

---

## API Endpoints

### Traditional Authentication

#### POST /auth/login
```json
Request:
{
  "username": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "CLIENT",
  "username": "user@example.com"
}
```

### OAuth2 Authentication

#### GET /oauth2/authorization/google
- Initiates Google OAuth2 login flow
- User will be redirected to Google

#### GET /oauth2/authorization/facebook
- Initiates Facebook OAuth2 login flow
- User will be redirected to Facebook

#### GET /oauth2/authorization/apple
- Initiates Apple Sign In flow
- User will be redirected to Apple

#### GET /oauth2/authorization/okta
- Initiates Okta OAuth2 login flow
- User will be redirected to Okta

#### GET /login/oauth2/code/{provider}
- OAuth2 callback endpoint (handled by Spring Security)
- Providers: google, facebook, apple, okta
- Users should not call this directly

---

## Frontend Integration

### React/Angular Example

```typescript
// Login with Google
function loginWithGoogle() {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
}

// Login with Facebook
function loginWithFacebook() {
  window.location.href = 'http://localhost:8080/oauth2/authorization/facebook';
}

// Login with Apple
function loginWithApple() {
  window.location.href = 'http://localhost:8080/oauth2/authorization/apple';
}

// Login with Okta
function loginWithOkta() {
  window.location.href = 'http://localhost:8080/oauth2/authorization/okta';
}

// OAuth2 Redirect Handler Component (at /oauth2/redirect)
function OAuth2RedirectHandler() {
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');

    if (token) {
      localStorage.setItem('jwt_token', token);
      navigate('/dashboard');
    }
  }, []);

  return <div>Loading...</div>;
}

// Using the JWT token
function makeApiCall() {
  const token = localStorage.getItem('jwt_token');

  fetch('http://localhost:8080/api/parcels', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
}
```

---

## Security Features

### 1. Stateless Authentication
- No server-side sessions
- JWT tokens contain all necessary information
- Scalable and cloud-ready

### 2. Unified Authorization
- OAuth2 users and traditional users have the same permissions
- Role-based access control (RBAC) works for both
- Single JWT validation filter

### 3. Account Linking
- If a user exists with the same email, OAuth2 provider is linked to existing account
- Prevents duplicate accounts

### 4. Default Role Assignment
- New OAuth2 users automatically get "CLIENT" role
- Can be changed by admin later

---

## Testing

### Test Traditional Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"client","password":"client123"}'
```

### Test Google OAuth2 Login
1. Open browser: `http://localhost:8080/oauth2/authorization/google`
2. Authorize with Google account
3. Check redirect URL for JWT token

### Test API Access with JWT
```bash
curl http://localhost:8080/api/parcels \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## Troubleshooting

### Issue: "CLIENT role not found"
**Solution**: Ensure your database has a role named "CLIENT". Run DataInitializer or insert manually.

### Issue: "Invalid redirect URI"
**Solution**: Add `http://localhost:8080/login/oauth2/code/google` to Google Cloud Console authorized redirect URIs.

### Issue: OAuth2 works but returns error
**Solution**: Check application logs. Ensure GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET are set correctly.

### Issue: Password cannot be null
**Solution**: Run Liquibase migration 007-add-oauth2-fields.xml to make password nullable.

---

## Production Deployment

### 1. Update Redirect URIs
Change OAuth2 redirect URIs in Google/Facebook console to production URL:
```
https://api.smartlogi.com/login/oauth2/code/google
https://api.smartlogi.com/login/oauth2/code/facebook
```

### 2. Set Environment Variables
```bash
export GOOGLE_CLIENT_ID=prod-client-id
export GOOGLE_CLIENT_SECRET=prod-client-secret
export FRONTEND_REDIRECT_URL=https://app.smartlogi.com/oauth2/redirect
```

### 3. Use HTTPS
OAuth2 providers require HTTPS in production. Use SSL/TLS certificates.

---

## Files Created/Modified

### New Files
- `AuthProvider.java` - Enum for authentication providers
- `CustomOAuth2UserService.java` - Handles OAuth2 user creation/update
- `CustomOAuth2User.java` - Wraps User entity for OAuth2
- `OAuth2AuthenticationSuccessHandler.java` - Generates JWT after OAuth2
- `OAuth2AuthenticationFailureHandler.java` - Handles OAuth2 errors
- `007-add-oauth2-fields.xml` - Database migration

### Modified Files
- `User.java` - Added OAuth2 fields
- `UserRepository.java` - Added OAuth2 query methods
- `SecurityConfig.java` - Added OAuth2 configuration
- `application.yml` - Added OAuth2 client configuration
- `pom.xml` - Added OAuth2 client dependency

---

## Next Steps

1. Test Google OAuth2 login
2. Test Facebook OAuth2 login
3. Implement frontend OAuth2 button
4. Add Swagger documentation
5. Create Postman collection
6. Docker deployment
7. Production deployment with HTTPS
