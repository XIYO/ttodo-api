# Security Notes

## Development Authentication

### Test Token for Development

For development and testing purposes, use the following token:

```bash
# Development token for anon@ttodo.dev (expires in 100 years)
export DEV_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJpYXQiOjE3NTY0OTcyMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJhbm9uQHR0b2RvLmRldiIsIm5pY2tuYW1lIjoi7J2166qF7IKs7Jqp7J6QIiwidGltZVpvbmUiOiJBc2lhL1Nlb3VsIiwibG9jYWxlIjoia29fS1IiLCJzY29wZSI6IlJPTEVfVVNFUiJ9.0omjGk_61raPaG4yof4tLGInII276NkzdS1rjRhf9erzXRFjMvQsbl-FAFWdll5l6YPEbmoSVLoXzCqDJU4X_fXhC6bAEUXIs4_2_IrgsxxpoWGC_KaTv6tCd-35EPb12AfSTkLHpaXlUjbmEkNiAZypD54ICfUY_6f3ts0Ki75GFjLJ0wGUju7vX8ECHljxLhyNt6H1XVgKGUxta1Fx_R1wcaiJZR0j0I7LW0JV3ZRbO1hG_3in9Y3eL5k-hYRSYLXJr6H6GNzY2ztbKru2tXVRJQFuGVrsx-RPzNmm-L5xb-DBRFrt6KDa1bQoedL12WgFTWwQe96Uk-DhoOyPhw"

# Use in curl (cookie method)
curl -H "Cookie: access-token=$DEV_TOKEN" http://localhost:8080/todos

# Use in curl (bearer method)
curl -H "Authorization: Bearer $DEV_TOKEN" http://localhost:8080/todos
```

**⚠️ Security Warning**

- This token is for **DEVELOPMENT ONLY**
- DO NOT use in production environments
- Token has extended expiry for development convenience
- Generate new tokens for production deployment

### Token Details

- User: anon@ttodo.dev (Anonymous test user)
- UUID: ffffffff-ffff-ffff-ffff-ffffffffffff  
- TimeZone: Asia/Seoul
- Locale: ko_KR
- Scope: ROLE_USER
- Device ID: test-device-anon

## Production Security Considerations

### JWT Configuration

- Use proper RSA key pairs for production
- Implement token rotation strategy
- Set appropriate expiry times (15-60 minutes for access tokens)
- Use refresh token rotation

### Rate Limiting

- Implement rate limiting for API endpoints
- Protect against brute force attacks
- Monitor unusual access patterns

### Data Protection

- Sanitize all user inputs using ValidationUtils
- Apply XSS protection on HTML content
- Validate SQL injection patterns
- Implement proper CORS policies

### Monitoring

- Log security events
- Monitor failed authentication attempts
- Track token usage patterns
- Alert on suspicious activities