# TTODO API

Enterprise-grade personal TODO management system with gamification features built on Spring Boot 3.5 and Domain-Driven Design architecture.

## Quick Start

### Prerequisites
- JDK 21
- Docker Desktop

### Run Application
```bash
git clone <repository-url>
cd ttodo-api
./gradlew bootRun
```

The application automatically starts PostgreSQL and Redis via Docker Compose integration.

### Access Points
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- API Docs: http://localhost:8080/v3/api-docs
- Health Check: http://localhost:8080/actuator/health

## Architecture

### Technology Stack
- **Runtime**: Java 21 LTS
- **Framework**: Spring Boot 3.5.6
- **Database**: PostgreSQL 17 (primary), Redis 7 (cache/sessions)
- **Security**: Spring Security with JWT (RSA-signed)
- **ORM**: Spring Data JPA with Hibernate
- **Documentation**: SpringDoc OpenAPI 3
- **Build**: Gradle 8
- **Testing**: JUnit 5, MockMvc, Mockito
- **Utilities**: Lombok, MapStruct, OWASP HTML Sanitizer

### Project Structure
```
src/main/java/point/ttodoApi/
├── auth/            # Authentication & Authorization
├── user/            # User Management
├── todo/            # Personal TODO Management
├── challenge/       # Challenge & Gamification
├── experience/      # XP & Level System
├── category/        # TODO Categorization
├── profile/         # User Profiles & Settings
├── level/           # Level Definitions
├── sync/            # Data Synchronization
└── shared/          # Common Utilities & Config
```

### Core Patterns
- **DDD Architecture**: Domain/Application/Infrastructure/Presentation layers
- **CQRS-lite**: Separated Command/Query services
- **Event-Driven**: Spring Events for cross-domain communication
- **Repository Pattern**: JPA with Specification for dynamic queries
- **DTO Mapping**: MapStruct for object transformations

## Features

### Authentication & Security
- JWT with RSA key pair signing
- HTTP-Only cookie-based tokens
- Redis-backed token storage and blacklisting
- Role-based access control

### TODO Management
- CRUD operations with full-text search
- Recurring tasks (RFC 5545 RRULE support)
- Virtual instances for repeat schedules
- Tag and priority system
- Calendar integration

### Gamification
- Challenge creation and participation
- XP and leveling system
- Leaderboards
- Achievement tracking

### Data & Search
- JPA Criteria API for type-safe queries
- Dynamic filtering with Specifications
- Pagination and sorting
- Full-text keyword search

### Quality Assurance
- Custom Bean Validation annotations
- OWASP HTML Sanitizer for XSS prevention
- RFC 7807 Problem Details error responses
- Comprehensive controller tests

## Development

### Database Configuration
```yaml
PostgreSQL (auto-started):
  Host: localhost
  Port: 5432
  Database: ttodo_dev
  Username: ttodo_user
  Password: ttodo_password

Redis (auto-started):
  Host: localhost
  Port: 6379
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "TodoControllerTest"

# Run tests by pattern
./gradlew test --tests "*ControllerTest"
```

### Docker Deployment
```bash
# Build application
./gradlew build

# Run with Docker Compose (local)
docker-compose -f docker-compose.local.yml up -d

# Run with Docker Compose (production)
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

### CI/CD
GitHub Actions workflow builds ARM64 images on push to main/develop and version tags.

Images are published to GitHub Container Registry:
```
ghcr.io/<owner>/<repository>:latest
ghcr.io/<owner>/<repository>:develop
ghcr.io/<owner>/<repository>:v1.0.0
```

## API Documentation

See [SECURITY_NOTES.md](SECURITY_NOTES.md) for development authentication tokens.

### Key Endpoints

**Authentication**
- POST `/auth/sign-up` - Register and auto-login
- POST `/auth/sign-in` - Login
- POST `/auth/sign-out` - Logout
- POST `/auth/refresh` - Refresh access token

**TODO Management**
- GET `/todos` - List todos with filters
- POST `/todos` - Create todo
- GET `/todos/{id}:{daysDifference}` - Get todo (supports virtual instances)
- PUT `/todos/{id}:{daysDifference}` - Update todo
- PATCH `/todos/{id}:{daysDifference}` - Partial update
- DELETE `/todos/{id}:{daysDifference}` - Delete/hide todo

**Challenges**
- GET `/challenges` - List challenges
- POST `/challenges` - Create challenge
- GET `/challenges/{id}` - Get challenge details
- PATCH `/challenges/{id}` - Update challenge
- DELETE `/challenges/{id}` - Delete challenge

**User & Profile**
- GET `/user/me` - Get current user
- PATCH `/user/{id}` - Update user info
- GET `/user/{id}/profile` - Get profile
- PATCH `/user/{id}/profile` - Update profile

For complete API reference, see Swagger UI.

## Input Format
This API accepts only form-encoded input:
- `application/x-www-form-urlencoded`
- `multipart/form-data`

JSON request bodies are **not** accepted. Complex data (e.g., recurrence rules) should be passed as JSON strings within form fields.

Example:
```bash
curl -X POST http://localhost:8080/todos \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "title=Exercise" \
  --data-urlencode 'recurrenceRuleJson={"frequency":"WEEKLY","interval":1}'
```

## Project Documentation
- [CLAUDE.md](CLAUDE.md) - LLM-optimized coding guidelines
- [PRD.md](PRD.md) - Product requirements and roadmap
- [SECURITY_NOTES.md](SECURITY_NOTES.md) - Security configuration

## License
See LICENSE file for details.
