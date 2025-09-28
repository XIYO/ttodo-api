# Repository Guidelines

## Project Structure & Module Organization
- Source: `src/main/java/point/ttodoApi` using DDD layers: `presentation/`, `application/`, `domain/`, `infrastructure/`, plus `shared/` for cross‑cutting concerns.
- Tests: `src/test/java/point/ttodoApi`. Shared base: `test/BaseIntegrationTest` (JUnit 5 + Testcontainers: Postgres/Redis).
- Config: `src/main/resources` (`application.yml`, `application-common-*.yml`, `log4j2-spring.xml`).
- Examples: `todo/presentation/TodoController.java`, `challenge/application/...`, `shared/error/...`.

## Build, Test, and Development Commands
- `./gradlew bootRun` — Run locally with `dev` profile. Swagger UI: http://localhost:8080/.
- `docker-compose up -d` — Start Postgres/Redis (if not using Spring Boot’s compose integration).
- `./gradlew test` — Run unit and integration tests.
- `./gradlew build` — Build jar and run all checks.
- `./gradlew dependencyUpdates` — View dependency upgrade suggestions.

## Coding Style & Naming Conventions
- Java 21, Spring Boot 3.5; 2‑space indentation; UTF‑8; LF endings.
- Packages lowercase (e.g., `point.ttodoApi.todo.presentation`); classes `PascalCase`; methods/fields `camelCase`.
- DTOs in `presentation/dto`: suffix `*Request`, `*Response`.
- Application layer: suffix `*Command`, `*Query`, `*Result`.
- MapStruct mappers in `.../presentation/mapper`: suffix `*Mapper`.
- Exceptions in `exception/` with suffix `*Exception`; reuse `shared/error` when possible.
- Lombok: prefer `@RequiredArgsConstructor` and `final` fields; avoid field injection.

## Testing Guidelines
- Frameworks: JUnit 5, Spring Boot Test, Testcontainers.
- Naming: `*Test.java` mirroring the package (e.g., `todo/presentation/TodoControllerTest.java`).
- Integration: extend `test/BaseIntegrationTest` for containers and JWT fixtures.
- Deterministic tests only; no reliance on local state. Run with `./gradlew test`.

## Commit & Pull Request Guidelines
- Conventional Commits (`feat`, `fix`, `refactor`, `test`, `style`, `docs`, `chore`). Example: `feat(todo): add pin order API`.
- Branches: `feature/<name>`, `fix/<name>`, `chore/<name>`.
- PRs: clear description, linked issues, reproduction/verification steps (curl or Swagger screenshots), note breaking changes, and update docs/Swagger annotations. CI must pass build and tests.

## Security & Configuration Tips
- Profiles: `dev` (default), `prod` via `SPRING_PROFILES_ACTIVE`.
- Secrets via environment variables; never commit credentials.
- Datastores via Docker (`docker-compose.yml`).
- Validate inputs (Bean Validation) and avoid logging sensitive data; Log4j2 is configured.

