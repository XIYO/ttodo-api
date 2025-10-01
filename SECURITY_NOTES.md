# Security Notes

## Development Environment

### Development Token

For development and testing, use the pre-configured test token:

```bash
# Development token for anon@ttodo.dev (expires in 100 years)
export DEV_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJpYXQiOjE3NTY0OTcyMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJhbm9uQHR0b2RvLmRldiIsIm5pY2tuYW1lIjoi7J2166qF7IKs7Jqp7J6QIiwidGltZVpvbmUiOiJBc2lhL1Nlb3VsIiwibG9jYWxlIjoia29fS1IiLCJzY29wZSI6IlJPTEVfVVNFUiJ9.0omjGk_61raPaG4yof4tLGInII276NkzdS1rjRhf9erzXRFjMvQsbl-FAFWdll5l6YPEbmoSVLoXzCqDJU4X_fXhC6bAEUXIs4_2_IrgsxxpoWGC_KaTv6tCd-35EPb12AfSTkLHpaXlUjbmEkNiAZypD54ICfUY_6f3ts0Ki75GFjLJ0wGUju7vX8ECHljxLhyNt6H1XVgKGUxta1Fx_R1wcaiJZR0j0I7LW0JV3ZRbO1hG_3in9Y3eL5k-hYRSYLXJr6H6GNzY2ztbKru2tXVRJQFuGVrsx-RPzNmm-L5xb-DBRFrt6KDa1bQoedL12WgFTWwQe96Uk-DhoOyPhw"

# Cookie-based authentication
curl -H "Cookie: access-token=$DEV_TOKEN" http://localhost:8080/todos

# Bearer token authentication
curl -H "Authorization: Bearer $DEV_TOKEN" http://localhost:8080/todos
```

**Token Details**:
- User: anon@ttodo.dev
- UUID: `ffffffff-ffff-ffff-ffff-ffffffffffff`
- Role: ROLE_USER
- Timezone: Asia/Seoul
- Locale: ko_KR
- Expiry: 100 years (development only)

**Warning**: This token is for DEVELOPMENT ONLY. Never use in production.

### Development Database Access

```bash
# PostgreSQL (auto-started via Docker Compose)
psql -h localhost -p 5432 -U ttodo_user -d ttodo_dev
# Password: ttodo_password

# Redis
redis-cli -h localhost -p 6379
```

## Current Security Implementation

### Authentication & Authorization

**JWT Configuration**:
- Algorithm: RSA256 with key pair signing
- Access token: 30 minutes expiry
- Refresh token: 7 days expiry
- Storage: HTTP-Only cookies (access-token, refresh-token)
- Blacklisting: Redis-based token revocation

**Token Resolution**:
- Supports both Cookie and Bearer header authentication
- MultiBearerTokenResolver for flexible client integration
- Automatic token refresh via `/auth/refresh` endpoint

### Input Validation

**XSS Prevention**:
- OWASP HTML Sanitizer for all user-generated HTML content
- Custom `@SafeHtml` validation annotation
- Automatic sanitization in ValidationUtils

**Bean Validation**:
- All request DTOs validated with Jakarta Bean Validation
- Custom validators: `@UniqueEmail`, `@ValidRecurrenceRule`
- Comprehensive error messages via RFC 7807 Problem Details

### Data Protection

**Encryption**:
- All data encrypted in transit (HTTPS required in production)
- Database credentials via environment variables
- RSA private key externalized (never in source code)

**Password Security**:
- BCrypt password hashing with Spring Security
- Minimum 8 characters enforced
- No password storage in logs or error messages

## Production Security Requirements

### Critical Security Hardening (Q2 2026 - Month 4)

**Token Rotation**:
- Implement refresh token rotation
- Device-based token management
- Suspicious login detection and blocking
- Token expiry optimization (15min access, 7d refresh)

**Rate Limiting**:
- Redis-based distributed rate limiter
- Per-user limits: 1000 req/hour
- Per-IP limits: 10000 req/hour
- Configurable limits per endpoint
- Rate limit headers in responses (X-RateLimit-*)

**Security Scanning**:
- OWASP Dependency Check in CI/CD pipeline
- Trivy container image scanning
- Regular penetration testing
- SAST/DAST integration

### Observability & Monitoring (Q2 2026 - Month 5)

**Security Event Logging**:
- Failed authentication attempts
- Suspicious access patterns
- Token usage anomalies
- Admin actions audit trail
- Structured JSON logging with security context

**Alerting**:
- Real-time alerts for security events
- Failed login threshold monitoring
- Unusual API access patterns
- Token abuse detection

### Compliance Requirements

**OWASP Top 10 Compliance**:
- A01 Broken Access Control: Role-based access enforced
- A02 Cryptographic Failures: TLS 1.3, strong ciphers
- A03 Injection: Parameterized queries, input validation
- A04 Insecure Design: Threat modeling completed
- A05 Security Misconfiguration: Secure defaults enforced
- A06 Vulnerable Components: Automated dependency scanning
- A07 Authentication Failures: Strong password policy, MFA planned
- A08 Software/Data Integrity: Signed artifacts, verified dependencies
- A09 Logging Failures: Comprehensive security event logging
- A10 SSRF: URL validation, allowlist enforcement

**Data Privacy**:
- GDPR compliance considerations (data export, deletion)
- User consent management
- Data minimization principles
- Audit logs for data access

## Security Best Practices

### Development Guidelines

1. **Never commit secrets**: Use environment variables or secret management
2. **Validate all inputs**: Trust nothing from clients
3. **Least privilege**: Grant minimum necessary permissions
4. **Fail securely**: Default deny, explicit allow
5. **Defense in depth**: Multiple security layers

### Deployment Checklist

- [ ] Change all default credentials
- [ ] Enable HTTPS only (disable HTTP)
- [ ] Configure CORS policies appropriately
- [ ] Set secure cookie flags (Secure, HttpOnly, SameSite)
- [ ] Enable security headers (CSP, HSTS, X-Frame-Options)
- [ ] Disable debug mode and verbose errors
- [ ] Configure rate limiting
- [ ] Set up monitoring and alerting
- [ ] Enable audit logging
- [ ] Perform security scan before deployment

### Incident Response Plan

**Detection**: Monitoring alerts → Security team notification → Investigation

**Containment**:
1. Identify affected systems and users
2. Isolate compromised components
3. Block suspicious IPs/tokens immediately

**Recovery**:
1. Patch vulnerabilities
2. Reset compromised credentials
3. Restore from secure backups if needed
4. Verify system integrity

**Post-Incident**:
1. Root cause analysis
2. Update security measures
3. Documentation and lessons learned
4. Communication to stakeholders

## Security Contact

For security vulnerabilities or concerns, contact the development team immediately.

**DO NOT** create public GitHub issues for security vulnerabilities.

---

**Document Version**: 2.0
**Last Updated**: 2025-10-02
**Next Review**: Q2 2026 (Security Hardening Phase)
