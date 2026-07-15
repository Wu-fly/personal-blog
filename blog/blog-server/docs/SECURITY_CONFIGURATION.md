# Spring Security and JWT Configuration

## Overview

This document describes the Spring Security and JWT configuration for the Personal Blog System backend.

## Components

### 1. JwtUtil (JWT工具类)

**Location:** `com.blog.security.JwtUtil`

**Purpose:** Provides JWT token generation, validation, and parsing functionality.

**Key Methods:**
- `generateToken(Long userId, String phone, String role)` - Generates a JWT token with 7-day expiration
- `validateToken(String token)` - Validates if a token is valid and not expired
- `isTokenExpired(String token)` - Checks if a token has expired
- `getUserIdFromToken(String token)` - Extracts user ID from token
- `getPhoneFromToken(String token)` - Extracts phone number from token
- `getRoleFromToken(String token)` - Extracts user role from token
- `refreshToken(String token)` - Generates a new token from an existing one

**Configuration:**
- Secret Key: Configured in `application.yml` as `jwt.secret`
- Expiration: 7 days (604800000 milliseconds) as per requirement 1.3
- Algorithm: HS512 (HMAC-SHA512)

### 2. JwtAuthenticationFilter (JWT认证过滤器)

**Location:** `com.blog.security.JwtAuthenticationFilter`

**Purpose:** Intercepts all HTTP requests to validate JWT tokens and set authentication context.

**Flow:**
1. Extract JWT token from `Authorization` header
2. Validate token using `JwtUtil`
3. If valid, extract user information (userId, phone, role)
4. Create `UsernamePasswordAuthenticationToken` with authorities
5. Set authentication in `SecurityContextHolder`

**Configuration:**
- Token Header: `Authorization` (configured in `application.yml`)
- Token Prefix: `Bearer ` (configured in `application.yml`)

### 3. JwtAuthenticationEntryPoint (认证入口点)

**Location:** `com.blog.security.JwtAuthenticationEntryPoint`

**Purpose:** Handles unauthorized access attempts by returning a standardized JSON error response.

**Response Format:**
```json
{
  "success": false,
  "errorCode": "UNAUTHORIZED",
  "message": "未授权访问，请先登录",
  "timestamp": "2025-12-25T10:30:00Z",
  "path": "/api/articles"
}
```

### 4. SecurityConfig (Spring Security配置)

**Location:** `com.blog.security.SecurityConfig`

**Purpose:** Configures Spring Security with JWT authentication, CORS, and authorization rules.

**Key Configurations:**

#### CSRF Protection
- **Status:** Disabled
- **Reason:** JWT tokens provide sufficient protection for stateless APIs

#### CORS Configuration
- **Allowed Origins:** All origins (using pattern matching)
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS, PATCH
- **Allowed Headers:** All headers
- **Allow Credentials:** Yes
- **Exposed Headers:** Authorization
- **Max Age:** 3600 seconds

#### Session Management
- **Policy:** STATELESS
- **Reason:** JWT-based authentication doesn't require server-side sessions

#### Authorization Rules

**Public Endpoints (No Authentication Required):**
- `/auth/**` - Authentication endpoints (login, register, send SMS)
- `GET /articles` - Article list (visitors can view titles and summaries)
- `GET /articles/search` - Article search
- `GET /categories/**` - Category endpoints
- `GET /tags/**` - Tag endpoints
- `GET /users/*/space` - Blogger personal space
- `GET /carousel` - Carousel configuration

**Admin Endpoints (ROLE_ADMIN Required):**
- `/admin/**` - All admin endpoints

**Blogger Endpoints (ROLE_BLOGGER or ROLE_ADMIN Required):**
- `POST /articles` - Create article
- `PUT /articles/**` - Update article
- `DELETE /articles/**` - Delete article
- `/users/space/settings` - Personal space settings

**Authenticated Endpoints:**
- All other endpoints require authentication (ROLE_USER, ROLE_BLOGGER, or ROLE_ADMIN)

### 5. CustomUserDetails (自定义用户详情)

**Location:** `com.blog.security.CustomUserDetails`

**Purpose:** Custom implementation of Spring Security's `UserDetails` interface.

**Fields:**
- `id` - User ID
- `phone` - Phone number (used as username)
- `password` - Encrypted password
- `role` - User role (USER, BLOGGER, ADMIN)
- `status` - Account status (ACTIVE, DISABLED)

## Security Flow

### 1. User Login Flow

```
1. User submits phone + SMS code
2. Backend validates SMS code
3. Backend generates JWT token using JwtUtil
4. Token returned to client with 7-day expiration
5. Client stores token in localStorage
```

### 2. Authenticated Request Flow

```
1. Client sends request with Authorization header: "Bearer <token>"
2. JwtAuthenticationFilter intercepts request
3. Filter extracts and validates token
4. If valid, user info extracted and authentication set
5. Request proceeds to controller
6. Controller checks method-level permissions (@PreAuthorize)
7. Response returned to client
```

### 3. Token Expiration Flow

```
1. Client sends request with expired token
2. JwtAuthenticationFilter validates token
3. Token validation fails (expired)
4. JwtAuthenticationEntryPoint returns 401 Unauthorized
5. Client redirects to login page
```

## Configuration Properties

Add these properties to `application.yml`:

```yaml
jwt:
  secret: YourSecretKeyForJWTTokenGenerationMustBeLongEnough
  expiration: 604800000  # 7 days in milliseconds
  header: Authorization
  prefix: Bearer 
```

## Password Encoding

- **Algorithm:** BCrypt
- **Bean:** `PasswordEncoder` configured in `SecurityConfig`
- **Usage:** Encode passwords before storing in database

## Testing

### Testing JWT Token Generation

```java
@Autowired
private JwtUtil jwtUtil;

@Test
void testGenerateToken() {
    String token = jwtUtil.generateToken(1L, "13800138000", "USER");
    assertNotNull(token);
    assertTrue(jwtUtil.validateToken(token));
}
```

### Testing Token Expiration

```java
@Test
void testTokenExpiration() {
    String token = "expired_token_here";
    assertTrue(jwtUtil.isTokenExpired(token));
}
```

### Testing Authentication Filter

```java
@Test
@WithMockUser(roles = "USER")
void testAuthenticatedEndpoint() throws Exception {
    mockMvc.perform(get("/api/users/profile")
            .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk());
}
```

## Security Best Practices

1. **Secret Key Management**
   - Use environment variables for production
   - Never commit secrets to version control
   - Rotate keys periodically

2. **Token Storage**
   - Store tokens in localStorage (not cookies for SPA)
   - Clear tokens on logout
   - Implement token refresh mechanism

3. **HTTPS**
   - Always use HTTPS in production
   - Tokens transmitted over HTTP are vulnerable

4. **Token Expiration**
   - 7-day expiration balances security and UX
   - Implement refresh token for better security

5. **Role-Based Access Control**
   - Use method-level security with `@PreAuthorize`
   - Validate permissions in service layer

## Requirements Mapping

- **Requirement 1.3:** JWT token generation with 7-day expiration
  - Implemented in `JwtUtil.generateToken()`
  - Configured in `application.yml` with `jwt.expiration: 604800000`

- **Requirement 1.9:** Token expiration validation
  - Implemented in `JwtUtil.validateToken()` and `isTokenExpired()`
  - Handled by `JwtAuthenticationFilter` and `JwtAuthenticationEntryPoint`

## Troubleshooting

### Issue: 401 Unauthorized on valid requests

**Solution:** Check if token is properly formatted with "Bearer " prefix

### Issue: CORS errors

**Solution:** Verify CORS configuration in `SecurityConfig.corsConfigurationSource()`

### Issue: Token validation fails

**Solution:** 
- Check if secret key matches between token generation and validation
- Verify token hasn't expired
- Check token format and signature

## Future Enhancements

1. **Refresh Token Mechanism**
   - Implement separate refresh tokens with longer expiration
   - Add refresh endpoint to generate new access tokens

2. **Token Blacklist**
   - Use Redis to store invalidated tokens
   - Check blacklist in authentication filter

3. **Rate Limiting**
   - Implement rate limiting for authentication endpoints
   - Prevent brute force attacks

4. **Multi-Factor Authentication**
   - Add optional MFA for sensitive operations
   - Support TOTP or SMS-based MFA
