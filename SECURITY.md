# Security Documentation

## Development Environment Testing

⚠️ **Warning**: The information in this document is for development environments only. 
Never use these tokens or keys in production.

### Development JWT Token

For development and testing purposes, you can obtain a JWT token using the following methods:

#### Method 1: Dynamic Token Generation (Recommended)

Use the dev token endpoint to generate a fresh token:

```bash
# Get a development token (expires in 24 hours)
curl -X GET http://localhost:8080/auth/dev-token
```

This will return a JSON response with an access token that can be used for testing.

#### Method 2: Authentication Flow

Use the standard authentication flow:

1. Sign up: `POST /auth/sign-up`
2. Sign in: `POST /auth/sign-in`  
3. The JWT tokens will be automatically set as HTTP-only cookies

### Using JWT Tokens for API Testing

**curl examples:**

```bash
# Using cookies (automatic after sign-in)
curl -H "Cookie: access-token=YOUR_TOKEN_HERE" \
     http://localhost:8080/todos

# Using Authorization header
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" \
     http://localhost:8080/todos
```

**Swagger UI:**

1. Navigate to: http://localhost:8080/swagger-ui.html
2. Click the "Authorize" button
3. Enter your token (without "Bearer" prefix)
4. Click "Authorize"

### Development User Account

The development environment includes a pre-configured anonymous user:

- User ID: `ffffffff-ffff-ffff-ffff-ffffffffffff`
- Email: `anon@ttodo.dev`
- Nickname: `익명사용자`
- TimeZone: `Asia/Seoul`
- Locale: `ko_KR`

### Security Notes

- Development tokens are signed with test keys located in `src/test/resources/ttodo/jwt/`
- These keys are different from production keys
- All development tokens should be rotated regularly
- Never commit production keys or tokens to version control
- Use environment variables for production JWT secrets

### JWT Key Management

Development uses RSA key pairs:
- Private key: Used for signing tokens (test environment only)
- Public key: Used for verifying tokens
- Key ID: `test-rsa-key-id`

Production environments should:
- Generate their own RSA key pairs
- Store private keys securely (e.g., HashiCorp Vault, AWS Secrets Manager)
- Rotate keys regularly
- Use different key IDs for each environment