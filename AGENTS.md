# Agent Guidelines

## DTO Naming
- Presentation layer DTOs must use `Request` and `Response` suffixes only.
  - Organize them under `presentation/dto/request` and `presentation/dto/response` where applicable.
- Application layer uses `command` for input and `result` for output DTOs.
- Domain layer keeps entity names without the `Dto` suffix.
- Use MapStruct interfaces to convert between layers to minimize boilerplate.

## Endpoint Paths
- Do **not** include version numbers or `/api` prefixes in controller `@RequestMapping` paths.
  - Base domain already reflects API usage (e.g., `api.zzic.dev`).
  - API version is supplied via request headers.
- Example root paths: `/members`, `/challenges`.

## Development
- After modifying code or documentation run project tests: `./gradlew test`.
- Keep these guidelines in mind for future contributions.
