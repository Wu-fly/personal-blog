# CORS Configuration Documentation

## Overview

Cross-Origin Resource Sharing (CORS) is configured to allow the frontend applications (user portal and admin portal) to make requests to the backend API from different origins.

## Configuration Location

CORS settings are defined in:
- **Configuration Class**: `src/main/java/com/blog/config/CorsConfig.java`
- **Properties File**: `src/main/resources/application.yml`

## Configuration Properties

### application.yml

```yaml
cors:
  allowed-origins:
    - http://localhost:5173  # User portal (Vite dev server)
    - http://localhost:5174  # Admin portal (Vite dev server)
    - http://localhost:3000  # Alternative dev port
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
    - PATCH
  allowed-headers:
    - Authorization
    - Content-Type
    - Accept
    - Origin
    - X-Requested-With
  exposed-headers:
    - Authorization
    - Content-Disposition
  allow-credentials: true
  max-age: 3600
```

## Configuration Details

### 1. Allowed Origins

**Purpose**: Specifies which frontend origins can access the API.

**Development**:
- `http://localhost:5173` - User portal (Vite default port)
- `http://localhost:5174` - Admin portal
- `http://localhost:3000` - Alternative development port

**Production**: Update to include your production domain(s):
```yaml
allowed-origins:
  - https://yourdomain.com
  - https://admin.yourdomain.com
```

### 2. Allowed Methods

**Purpose**: Specifies which HTTP methods are allowed for cross-origin requests.

**Configured Methods**:
- `GET` - Retrieve resources
- `POST` - Create resources
- `PUT` - Update resources (full replacement)
- `DELETE` - Delete resources
- `OPTIONS` - Preflight requests
- `PATCH` - Partial updates

### 3. Allowed Headers

**Purpose**: Specifies which request headers can be used in cross-origin requests.

**Configured Headers**:
- `Authorization` - JWT token for authentication
- `Content-Type` - Request body format (e.g., application/json)
- `Accept` - Response format preference
- `Origin` - Request origin
- `X-Requested-With` - AJAX request indicator

### 4. Exposed Headers

**Purpose**: Specifies which response headers the frontend can access.

**Configured Headers**:
- `Authorization` - For token refresh scenarios
- `Content-Disposition` - For file downloads

### 5. Allow Credentials

**Purpose**: Enables sending cookies and authorization headers with cross-origin requests.

**Value**: `true`

**Important**: When `allowCredentials` is `true`, you cannot use wildcard (`*`) for allowed origins. You must specify exact origins.

### 6. Max Age

**Purpose**: Specifies how long (in seconds) the browser can cache preflight request results.

**Value**: `3600` (1 hour)

**Benefit**: Reduces the number of preflight OPTIONS requests, improving performance.

## How CORS Works

### Preflight Request (OPTIONS)

For certain requests (e.g., POST with custom headers), the browser sends a preflight OPTIONS request first:

```http
OPTIONS /api/articles HTTP/1.1
Origin: http://localhost:5173
Access-Control-Request-Method: POST
Access-Control-Request-Headers: Authorization, Content-Type
```

The server responds with allowed methods and headers:

```http
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: Authorization, Content-Type, Accept, Origin, X-Requested-With
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

### Actual Request

After the preflight succeeds, the browser sends the actual request:

```http
POST /api/articles HTTP/1.1
Origin: http://localhost:5173
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "My Article",
  "content": "Article content..."
}
```

The server includes CORS headers in the response:

```http
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Content-Type: application/json

{
  "success": true,
  "data": { ... }
}
```

## Environment-Specific Configuration

### Development

Use `application.yml` for local development with localhost origins.

### Production

Create `application-prod.yml` with production domains:

```yaml
cors:
  allowed-origins:
    - https://yourdomain.com
    - https://admin.yourdomain.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
    - PATCH
  allowed-headers:
    - Authorization
    - Content-Type
    - Accept
    - Origin
    - X-Requested-With
  exposed-headers:
    - Authorization
    - Content-Disposition
  allow-credentials: true
  max-age: 3600
```

Run with production profile:
```bash
java -jar blog-server.jar --spring.profiles.active=prod
```

## Testing CORS

### Unit Tests

Run the CORS configuration tests:

```bash
mvn test -Dtest=CorsConfigTest
```

### Manual Testing

Use curl to test CORS:

```bash
# Test preflight request
curl -X OPTIONS http://localhost:8080/api/articles \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization, Content-Type" \
  -v

# Test actual request
curl -X GET http://localhost:8080/api/articles \
  -H "Origin: http://localhost:5173" \
  -v
```

### Browser Testing

1. Open browser developer tools (F12)
2. Go to Network tab
3. Make a request from the frontend
4. Check the request headers include `Origin`
5. Check the response headers include `Access-Control-Allow-Origin`

## Common Issues and Solutions

### Issue 1: "No 'Access-Control-Allow-Origin' header is present"

**Cause**: The origin is not in the allowed origins list.

**Solution**: Add the origin to `cors.allowed-origins` in `application.yml`.

### Issue 2: "Credentials flag is 'true', but the 'Access-Control-Allow-Credentials' header is ''"

**Cause**: `allowCredentials` is not set to `true`.

**Solution**: Ensure `cors.allow-credentials: true` in `application.yml`.

### Issue 3: "The value of the 'Access-Control-Allow-Origin' header must not be the wildcard '*'"

**Cause**: Using wildcard origin with credentials enabled.

**Solution**: Specify exact origins instead of using `*`.

### Issue 4: Preflight request fails with 401 Unauthorized

**Cause**: Security configuration is blocking OPTIONS requests.

**Solution**: Ensure OPTIONS requests are permitted in `SecurityConfig`:

```java
.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

## Security Considerations

### 1. Restrict Origins in Production

Never use wildcard (`*`) for allowed origins in production. Always specify exact domains:

```yaml
allowed-origins:
  - https://yourdomain.com
  - https://admin.yourdomain.com
```

### 2. Limit Exposed Headers

Only expose headers that the frontend actually needs:

```yaml
exposed-headers:
  - Authorization
  - Content-Disposition
```

### 3. Use HTTPS in Production

Always use HTTPS for production origins:

```yaml
allowed-origins:
  - https://yourdomain.com  # ✓ Secure
  - http://yourdomain.com   # ✗ Insecure
```

### 4. Review Allowed Methods

Only allow HTTP methods that your API actually uses:

```yaml
allowed-methods:
  - GET
  - POST
  - PUT
  - DELETE
  - OPTIONS
  # Don't include TRACE, CONNECT, etc.
```

## Integration with Frontend

### Axios Configuration (Frontend)

The frontend Axios client should be configured to send credentials:

```javascript
// src/utils/request.js
import axios from 'axios';

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
  withCredentials: true  // Enable credentials
});
```

### Fetch API (Alternative)

If using Fetch API:

```javascript
fetch('http://localhost:8080/api/articles', {
  method: 'GET',
  credentials: 'include',  // Enable credentials
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  }
});
```

## Verification Checklist

- [ ] CORS configuration is loaded from `application.yml`
- [ ] Allowed origins include all frontend application URLs
- [ ] Allowed methods include all required HTTP methods
- [ ] Allowed headers include `Authorization` and `Content-Type`
- [ ] Credentials support is enabled (`allow-credentials: true`)
- [ ] Max age is set for preflight caching
- [ ] Unit tests pass successfully
- [ ] Manual testing with curl works
- [ ] Frontend can make cross-origin requests
- [ ] Production configuration uses HTTPS origins only

## References

- [Spring CORS Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-cors)
- [MDN CORS Guide](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [CORS Specification](https://fetch.spec.whatwg.org/#http-cors-protocol)

## Related Requirements

This configuration satisfies **Requirement 12.4**:
> THE 后端服务 SHALL 支持跨域请求（CORS）
