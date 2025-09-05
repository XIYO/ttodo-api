# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TTODO-API is a Spring Boot 3.5 backend API for personal TODO management with challenge and gamification features, following Domain-Driven Design (DDD) architecture.

**Tech Stack**: Java 21, Spring Boot 3.5, PostgreSQL, Redis, JWT authentication, Testcontainers

## Essential Commands

### Build & Run

```bash
# Run application (auto-starts PostgreSQL via Docker Compose)
./gradlew bootRun

# Build project
./gradlew build

# Run all tests (uses Testcontainers for PostgreSQL/Redis)
./gradlew test

# Run specific test class
./gradlew test --tests "TodoServiceTest"

# Run tests by pattern
./gradlew test --tests "*ServiceTest"     # Unit tests
./gradlew test --tests "*ControllerTest"  # Controller tests
./gradlew test --tests "*IntegrationTest" # Integration tests

# Clean and build
./gradlew clean build

# Check dependencies
./gradlew dependencies
```

### Docker Operations

```bash
# Local development with Docker Compose
docker-compose -f docker-compose.local.yml up -d

# Production deployment
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# View logs
docker-compose -f docker-compose.local.yml logs -f ttodo-api

# Stop services
docker-compose -f docker-compose.local.yml down

# Stop and remove volumes
docker-compose -f docker-compose.local.yml down -v
```

### Database Access

```bash
# Connect to PostgreSQL (dev environment)
psql -h localhost -p 5432 -U ttodo_user -d ttodo_dev
# Password: ttodo_password

# Connect to Redis
redis-cli -h localhost -p 6379
```

## Architecture & Structure

### DDD Package Organization

```
point.ttodoApi.[domain]/
├── domain/              # Entities, value objects, domain services
├── application/         # Application services, commands/queries, DTOs
│   ├── command/        # Command objects for write operations
│   ├── dto/            # Internal DTOs, results
│   └── event/          # Domain events
├── infrastructure/      # Repository implementations, external integrations
│   └── persistence/    # JPA repositories, specifications
├── presentation/        # REST controllers, request/response DTOs
│   ├── dto/           
│   │   ├── request/   # API request DTOs
│   │   └── response/  # API response DTOs
│   └── mapper/        # MapStruct mappers
└── exception/          # Domain-specific exceptions
```

### Key Domains

- **auth**: JWT authentication with RSA keys, Redis token storage, cookie-based auth
- **member**: User management with role-based access
- **todo**: Personal TODO CRUD with recurrence rules (based on RFC 5545)
- **challenge**: Challenge creation/participation with periods and visibility
- **experience**: XP/level system with event-driven updates
- **category**: TODO categorization with collaboration features
- **level**: Predefined level progression (1-100)

### Core Technical Patterns

1. **JWT Authentication**: 
   - RSA key pair for signing/verification
   - HTTP-Only cookies for tokens (access-token, refresh-token)
   - Redis for token blacklisting and refresh token storage
   - MultiBearerTokenResolver for both Cookie and Bearer header support

2. **Dynamic Queries**: 
   - JPA Criteria API for type-safe queries
   - Specification pattern for search filters
   - Dedicated search endpoints with dynamic parameters

3. **Event-Driven Updates**:
   - Spring Events for cross-domain communication
   - Example: TodoCompletedEvent → ExperienceEventHandler → XP update

4. **Validation System**:
   - Custom validators (e.g., `@UniqueEmail`, `@SafeHtml`)
   - OWASP HTML Sanitizer for XSS prevention
   - Bean Validation for request DTOs

5. **Error Handling**:
   - GlobalExceptionHandler with RFC 7807 problem details
   - Consistent error response format
   - Domain-specific exceptions

6. **Testing Strategy**:
   - BaseIntegrationTest with Testcontainers (PostgreSQL + Redis)
   - Test security annotations: `@WithMockMemberPrincipal`, `@WithAnonUser`
   - Pre-configured test JWT tokens in application-test.yml

## Development Workflow

### API Development Flow

1. **Swagger UI**: http://localhost:8080/swagger-ui/index.html
2. **Test with dev token**: Use pre-configured anon user token (100-year expiry) for development
3. **Authentication**: Sign-up → Auto-login → Cookies set automatically

### Test Token for Development

```bash
# Development token for anon@ttodo.dev (expires in 100 years)
export DEV_TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJpYXQiOjE3NTY0OTcyMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJhbm9uQHR0b2RvLmRldiIsIm5pY2tuYW1lIjoi7J2166qF7IKs7Jqp7J6QIiwidGltZVpvbmUiOiJBc2lhL1Nlb3VsIiwibG9jYWxlIjoia29fS1IiLCJzY29wZSI6IlJPTEVfVVNFUiJ9.0omjGk_61raPaG4yof4tLGInII276NkzdS1rjRhf9erzXRFjMvQsbl-FAFWdll5l6YPEbmoSVLoXzCqDJU4X_fXhC6bAEUXIs4_2_IrgsxxpoWGC_KaTv6tCd-35EPb12AfSTkLHpaXlUjbmEkNiAZypD54ICfUY_6f3ts0Ki75GFjLJ0wGUju7vX8ECHljxLhyNt6H1XVgKGUxta1Fx_R1wcaiJZR0j0I7LW0JV3ZRbO1hG_3in9Y3eL5k-hYRSYLXJr6H6GNzY2ztbKru2tXVRJQFuGVrsx-RPzNmm-L5xb-DBRFrt6KDa1bQoedL12WgFTWwQe96Uk-DhoOyPhw"

# Use in curl (cookie method)
curl -H "Cookie: access-token=$DEV_TOKEN" http://localhost:8080/todos

# Use in curl (bearer method)
curl -H "Authorization: Bearer $DEV_TOKEN" http://localhost:8080/todos
```

### Common Debugging

```bash
# Application logs location
tail -f logs/app.log

# Enable SQL logging (already configured in dev profile)
# See: src/main/resources/application-common-dev.yml

# View current Spring profile
echo $SPRING_PROFILES_ACTIVE  # Should be 'dev' for local development
```

## Code Conventions

### Service Layer
- `@Transactional` on write operations
- Return domain objects or result DTOs
- Throw domain exceptions (handled by GlobalExceptionHandler)
- Use application commands/queries for complex operations

### Repository Layer
- Extend `JpaRepository<Entity, UUID>` for basic CRUD
- Create Specification classes for dynamic queries
- Use `@Query` with JPQL for complex queries
- Repository methods return domain entities

### Controller Layer
- `@RestController` + `@RequestMapping`
- HTTP status codes: 201 (created), 204 (no content), 200 (success)
- Use `@Valid` for request validation
- Map exceptions to appropriate HTTP responses

### DTO & Mapping Strategy
- **Request DTOs**: `presentation/dto/request/` - API input validation
- **Response DTOs**: `presentation/dto/response/` - API output formatting
- **Commands/Queries**: `application/command/` and `application/query/` - Business operations
- **MapStruct**: For DTO ↔ Domain mapping (see `*PresentationMapper` classes)

### Testing Patterns
- Extend `BaseIntegrationTest` for integration tests (provides PostgreSQL/Redis containers)
- Use `@WithMockMemberPrincipal` for authenticated endpoint tests
- Test data uses UUIDs: `ffffffff-ffff-ffff-ffff-ffffffffffff` for anonymous user
- Testcontainers automatically manage database lifecycle

## Current Improvement Areas

From README.md DDD improvements section:
- **DTO separation**: Move from using application commands directly to presentation-specific DTOs
- **API versioning**: Implement consistent `/api/v1/` prefix
- **RESTful design**: Convert RPC-style endpoints (`/join`, `/leave`) to resource-based
- **Filtering standardization**: Use query parameters instead of separate endpoints (`/completed`, `/uncompleted`)
- **HTTP status consistency**: Ensure 201 for creation, 204 for successful no-content operations

## Environment Configuration

### Development (auto-configured)
```yaml
# PostgreSQL (Docker Compose auto-starts)
POSTGRES_HOST: localhost
POSTGRES_PORT: 5432
POSTGRES_DB: ttodo_dev
POSTGRES_USER: ttodo_user
POSTGRES_PASSWORD: ttodo_password

# Redis (Docker Compose auto-starts)
REDIS_HOST: localhost
REDIS_PORT: 6379
```

### Production (.env.prod)
```bash
GITHUB_REPOSITORY=your-repo/ttodo-api
POSTGRES_PASSWORD=secure_password
JWT_SECRET_KEY=your_secret_key
```

## Key Endpoints

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

## Form-Based Input Pattern

This API accepts only `application/x-www-form-urlencoded` or `multipart/form-data`:

```bash
# Example: Create TODO with recurrence rule
curl -X POST http://localhost:8080/todos \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Cookie: access-token=$DEV_TOKEN" \
  --data-urlencode "title=Weekly Exercise" \
  --data-urlencode "priorityId=1" \
  --data-urlencode "date=2025-01-01" \
  --data-urlencode 'recurrenceRuleJson={"frequency":"WEEKLY","interval":1,"byWeekDays":["MO","WE","FR"]}'
```

Note: `recurrenceRuleJson` is a JSON string field within the form data, not a JSON request body.