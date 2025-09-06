# Repository Guidelines

## Project Structure & Module Organization

- Source: `src/main/java/point/ttodoApi` following DDD layers: `presentation/`, `application/`, `domain/`,
  `infrastructure/`, plus `shared/` for cross-cutting concerns.
- Tests: `src/test/java/point/ttodoApi` (unit and integration). Shared test base: `test/BaseIntegrationTest` (JUnit 5 +
  Testcontainers for Postgres/Redis).
- Config: `src/main/resources` (`application.yml`, `application-common-*.yml`, `log4j2-spring.xml`).
- Examples: `todo/presentation/TodoController.java`, `challenge/application/...`, `shared/error/...`.

## Build, Test, and Development Commands

- `./gradlew bootRun`: Run locally with `dev` profile. Swagger UI at `http://localhost:8080/`.
- `docker-compose up -d`: Start Postgres/Redis locally (if not using Spring Boot’s compose integration).
- `./gradlew test`: Run tests (JUnit 5, Testcontainers).
- `./gradlew build`: Build jar and run all checks.
- `./gradlew dependencyUpdates`: Show dependency upgrade suggestions.

## Coding Style & Naming Conventions

- Language: Java 21, Spring Boot 3.5.
- Indentation: 2 spaces; UTF-8; Unix line endings.
- Packages: lowercase (`point.ttodoApi.todo.presentation`).
- Classes: `PascalCase`; methods/fields: `camelCase`.
- DTOs: request/response in `presentation/dto` with suffix `*Request`, `*Response`.
- Application layer: suffix `*Command`, `*Query`, `*Result`.
- Mapper: MapStruct mappers in `.../presentation/mapper` (suffix `*Mapper`).
- Exceptions: domain-specific in module `exception/` (suffix `*Exception`); reuse `shared/error` where possible.
- Lombok: prefer `@RequiredArgsConstructor` and `final` fields; avoid field injection.

## Testing Guidelines

- Frameworks: JUnit 5, Spring Boot Test, Testcontainers.
- Conventions: `*Test.java` under the mirrored package (e.g., `todo/presentation/TodoControllerTest.java`).
- Integration: extend `test/BaseIntegrationTest` to get Postgres/Redis containers and JWT fixtures.
- Run: `./gradlew test`. Ensure tests are deterministic and don’t rely on local state.

## Commit & Pull Request Guidelines

- Style: Conventional Commits (`feat`, `fix`, `refactor`, `test`, `style`, `docs`, `chore`).
    - Example: `feat(todo): add pin order API`.
- Branches: `feature/<name>`, `fix/<name>`, `chore/<name>`.
- PRs: clear description, linked issues, reproduction/verification steps (curl or Swagger screenshots), note breaking
  changes, and update docs/Swagger annotations.
- CI: PRs must pass build and tests.

## Security & Configuration Tips

- Profiles: `dev` (default), `prod` via `SPRING_PROFILES_ACTIVE`.
- Secrets via env vars; never commit credentials. Datastores run via Docker (`docker-compose.yml`).
- Validate inputs (Bean Validation) and avoid logging sensitive data; Log4j2 is configured.
