# TTODO-API Documentation Index

## 📚 Documentation Structure

```
docs/
├── INDEX.md                    # This file - Navigation hub
├── JWT_AUTHENTICATION.md       # JWT auth system details
├── API_REFERENCE.md           # Complete API documentation
└── ARCHITECTURE.md            # System architecture (future)
```

## 🚀 Quick Start

### For Developers

1. [Project Setup](../README.md#getting-started) - Initial setup and configuration
2. [JWT Authentication](./JWT_AUTHENTICATION.md) - Understanding auth flow
3. [API Reference](./API_REFERENCE.md) - Endpoint documentation
4. [Testing Guide](../README.md#testing) - Running tests

### For API Consumers

1. [API Reference](./API_REFERENCE.md) - Complete endpoint guide
2. [Authentication](./JWT_AUTHENTICATION.md#api-endpoints) - How to authenticate
3. [Swagger UI](http://localhost:8080/swagger-ui/index.html) - Interactive API testing

## 📖 Core Documentation

### System Architecture

#### Domain-Driven Design Structure

```
src/main/java/point/ttodoApi/
├── auth/                 # Authentication & Authorization
│   ├── domain/          # Entities, value objects
│   ├── application/     # Services, commands
│   ├── infrastructure/  # JWT, security implementations
│   └── presentation/    # Controllers, DTOs
├── todo/                # Todo management
├── category/            # Category system
├── challenge/           # Gamification challenges
├── member/              # User management
├── profile/             # User profiles
└── experience/          # XP and leveling system
```

### Key Components

#### 🔐 [JWT Authentication System](./JWT_AUTHENTICATION.md)

- Stateless authentication using JWT tokens
- RSA-256 signing with public/private keys
- HTTP-only cookies for security
- Automatic token refresh mechanism

#### 🌐 [REST API](./API_REFERENCE.md)

- RESTful endpoint design
- Consistent error handling (RFC 7807)
- Pagination support
- Swagger documentation

#### 💾 Data Layer

- **PostgreSQL**: Main database
- **Redis**: Token storage and caching
- **JPA/Hibernate**: ORM with Criteria API
- **Testcontainers**: Integration testing

## 📋 Project Files

### Configuration Files

| File                                    | Purpose                    |
|-----------------------------------------|----------------------------|
| [`README.md`](../README.md)             | Project overview and setup |
| [`CLAUDE.md`](../CLAUDE.md)             | AI assistant instructions  |
| [`PORTFOLIO.md`](../PORTFOLIO.md)       | Portfolio documentation    |
| [`INTERVIEW_QA.md`](../INTERVIEW_QA.md) | Technical interview Q&A    |

### Application Configuration

| File                          | Environment | Purpose            |
|-------------------------------|-------------|--------------------|
| `application.yml`             | All         | Base configuration |
| `application-common-dev.yml`  | Development | Dev settings       |
| `application-common-prod.yml` | Production  | Prod settings      |
| `application-local.yml`       | Local       | Local overrides    |

### Docker Configuration

| File                       | Purpose                 |
|----------------------------|-------------------------|
| `docker-compose.local.yml` | Local development stack |
| `docker-compose.prod.yml`  | Production deployment   |
| `Dockerfile`               | Application container   |

## 🔧 Development Workflow

### Common Commands

```bash
# Development
./gradlew bootRun              # Run application
./gradlew test                 # Run tests
./gradlew build               # Build project

# Docker
docker-compose -f docker-compose.local.yml up -d
docker-compose -f docker-compose.local.yml logs -f

# Database
psql -h localhost -p 5432 -U ttodo_user -d ttodo_dev
```

### API Testing

1. **Swagger UI**: http://localhost:8080/swagger-ui/index.html
2. **Get Dev Token**: `GET /auth/dev-token`
3. **Test Endpoints**: Use token in Authorization header

### Testing Strategy

```
tests/
├── Unit Tests          # Service layer tests
├── Integration Tests   # With Testcontainers
└── JWT Tests          # Authentication tests
```

## 🏗️ Architecture Patterns

### Domain-Driven Design (DDD)

- Clear separation of concerns
- Domain entities with business logic
- Application services for orchestration
- Infrastructure for external integrations

### Security Patterns

- JWT for stateless authentication
- HTTP-only cookies for XSS prevention
- CORS configuration for cross-origin requests
- Method-level security with `@PreAuthorize`

### Testing Patterns

- Testcontainers for integration tests
- Property-based token configuration
- MockMvc for controller tests
- Comprehensive JWT authentication tests

## 📊 API Statistics

### Endpoint Categories

- **Authentication**: 5 endpoints
- **Todo Management**: 12 endpoints
- **Categories**: 6 endpoints
- **Challenges**: 8 endpoints
- **Profiles**: 3 endpoints
- **Statistics**: 4 endpoints
- **Total**: 38+ endpoints

### Security Coverage

- ✅ 85% endpoints require authentication
- ✅ JWT token validation on all protected routes
- ✅ Role-based access control
- ✅ XSS and CSRF protection

## 🔄 Recent Updates

### Authentication System (2025-08-30)

- Simplified JWT tests from 34 to 6 essential tests
- Migrated tokens to property files
- Removed redundant test helpers
- Improved test maintainability

### Project Structure

- Consolidated authentication logic
- Standardized DTO patterns
- Improved error handling
- Enhanced Swagger documentation

## 📚 Additional Resources

### Internal Documentation

- [Codebase Guide](../CLAUDE.md) - For AI assistants
- [Interview Prep](../INTERVIEW_QA.md) - Technical Q&A
- [Portfolio](../PORTFOLIO.md) - Project showcase

### External Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security JWT](https://docs.spring.io/spring-security/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)

## 🤝 Contributing

### Code Standards

- Follow DDD architecture patterns
- Write comprehensive tests
- Document API changes
- Update Swagger annotations

### Git Workflow

```bash
git checkout -b feature/your-feature
# Make changes
./gradlew test
git commit -m "feat: your feature"
git push origin feature/your-feature
```

## 📝 Notes

### Known Issues

- API versioning needs standardization
- Some endpoints use RPC-style naming
- DTO separation could be improved

### Planned Improvements

- [ ] Add `/api/v1/` prefix consistently
- [ ] Implement rate limiting
- [ ] Add OAuth2 social login
- [ ] Enhance monitoring and logging
- [ ] Implement WebSocket for real-time updates

---

*Last Updated: 2025-08-30*
*Generated with Claude Code*