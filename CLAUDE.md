# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TTODO-API is a Spring Boot backend API server for personal TODO management with challenge and gamification features. The project follows Domain-Driven Design (DDD) architecture with clear separation of concerns.

## Essential Commands

### Build & Run

```bash
# Run application (auto-starts PostgreSQL via Docker Compose)
./gradlew bootRun

# Build project
./gradlew build

# Run tests (uses Testcontainers for PostgreSQL)
./gradlew test

# Run specific test class
./gradlew test --tests "TodoServiceTest"

# Run tests with specific pattern
./gradlew test --tests "*ServiceTest"

# Clean and build
./gradlew clean build
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
```

## Architecture & Structure

### DDD-Based Package Organization

Each domain follows a consistent structure:
- **domain**: Entities, value objects, domain services
- **application**: Application services, commands/queries, DTOs
- **infrastructure**: Repository implementations, external integrations
- **presentation**: REST controllers, request/response DTOs

### Key Domains

- **auth**: JWT-based authentication with Redis token storage
- **member**: User management
- **todo**: Personal TODO CRUD with dynamic queries
- **challenge**: Challenge creation and participation
- **experience**: Gamification with levels and XP
- **category**: TODO categorization with collaboration
- **level**: Level progression system

### Core Technical Patterns

1. **Authentication**: JWT tokens in HTTP-Only cookies with refresh mechanism
2. **Data Access**: Spring Data JPA with Criteria API for type-safe queries
3. **Validation**: Custom validators with XSS prevention (OWASP sanitizer)
4. **Error Handling**: GlobalExceptionHandler with RFC 7807 problem details
5. **Security**: Spring Security with method-level authorization
6. **Caching**: Redis for token storage and session management
7. **Testing**: Testcontainers for integration tests

### Database Schema

- **PostgreSQL** main database with JPA entities
- **Redis** for token storage and caching
- Database initialization handled by Spring Boot on startup
- Schema updates via JPA DDL auto-update in dev, migrations in prod

## Development Workflow

### API Development

1. **Check Swagger UI**: http://localhost:8080/swagger-ui/index.html
2. **Test endpoints**: Use Swagger UI or curl/Postman
3. **Authentication flow**: Sign-up → Auto-login → Use cookies for subsequent requests

### Testing Strategy

```bash
# Unit tests for services
./gradlew test --tests "*ServiceTest"

# Integration tests with real database
./gradlew test --tests "*IntegrationTest"

# Controller tests
./gradlew test --tests "*ControllerTest"
```

### Common Debugging

```bash
# Check application logs
tail -f logs/app.log

# View SQL queries (enabled in dev profile)
# Check application-common-dev.yml for logging configuration

# Connect to PostgreSQL
psql -h localhost -p 5432 -U ttodo_user -d ttodo_dev
# Password: ttodo_password
```

## Code Conventions

### Service Layer
- Use `@Transactional` for write operations
- Return domain results, map to DTOs in presentation layer
- Throw domain exceptions, handle in GlobalExceptionHandler

### Repository Layer
- Extend `JpaRepository` for basic CRUD
- Use `@Query` for complex queries
- Implement custom repositories with Criteria API for dynamic queries

### Controller Layer
- Use `@RestController` with `@RequestMapping`
- Return appropriate HTTP status codes
- Use `@Valid` for request validation
- Path variables for resource IDs

### DTO Patterns
- Request/Response DTOs in presentation layer
- Command/Query objects in application layer
- Use MapStruct for object mapping

## Current Improvement Areas (from README)

- **DTO separation**: Create presentation-specific DTOs instead of using application commands directly
- **API versioning**: Add `/api/v1/` prefix consistently
- **RESTful design**: Replace RPC-style endpoints with resource-based URLs
- **Standardize filtering**: Use query parameters consistently instead of separate endpoints
- **HTTP status codes**: Use 201 for creation, 204 for no content consistently

## Environment Variables

### Development
```yaml
# Auto-configured in application-common-dev.yml
POSTGRES_HOST: localhost
POSTGRES_PORT: 5432
POSTGRES_DB: ttodo_dev
POSTGRES_USER: ttodo_user
POSTGRES_PASSWORD: ttodo_password
REDIS_HOST: localhost
REDIS_PORT: 6379
```

### Production
Configure in `.env.prod`:
```
GITHUB_REPOSITORY=your-repo/ttodo-api
POSTGRES_PASSWORD=secure_password
JWT_SECRET_KEY=your_secret_key
```

## Useful Resources

- **API Documentation**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics