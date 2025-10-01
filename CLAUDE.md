# CLAUDE.md

LLM-optimized coding guidelines and architecture rules for TTODO API development.

## Project Context

TTODO API: Enterprise TODO management system with gamification
- Stack: Java 21, Spring Boot 3.5.6, PostgreSQL 17, Redis 7
- Architecture: Domain-Driven Design with CQRS-lite pattern
- Authentication: JWT with RSA signing, HTTP-Only cookies

## Core Development Principles

### 1. Always Use Latest Versions
- Spring Framework family packages take precedence
- Dependencies must use latest stable versions
- Never downgrade versions without explicit reason
- Regular dependency updates via Gradle version catalog

### 2. Minimize Boilerplate
- Use Lombok for all data classes (entities, DTOs, value objects)
- MapStruct for all DTO conversions
- Spring Framework features before third-party libraries
- Exception: Test code may be verbose for independence and clarity

### 3. Spring Family First
- If Spring provides a feature, use it instead of third-party alternatives
- Examples:
  - Spring Data JPA (not QueryDSL)
  - Spring Security (not Apache Shiro)
  - Spring Validation (not Hibernate Validator annotations beyond basics)
  - Spring Events (not Guava EventBus)

### 4. Modern Java Syntax
- Use Java 21 features: records, pattern matching, switch expressions, text blocks
- Stream API and Optional for functional programming
- var for local variable type inference where clarity maintained

### 5. Architecture Adherence
- Strict DDD layer separation
- CQRS-lite: separate CommandService and QueryService
- No cross-layer violations
- See architecture rules below

## Essential Commands

### Build & Run
```bash
./gradlew bootRun          # Run with dev profile, auto-starts PostgreSQL/Redis
./gradlew build            # Clean build
./gradlew test             # Run all tests
./gradlew test --tests "*ControllerTest"  # Controller slice tests only
```

### Database
```bash
# PostgreSQL (auto-started by Spring Boot Docker Compose)
psql -h localhost -p 5432 -U ttodo_user -d ttodo_dev
# Password: ttodo_password

# Redis
redis-cli -h localhost -p 6379
```

## Architecture Rules

### Package Structure (Strict DDD)
```
point.ttodoApi.[domain]/
├── domain/              # Core business logic
│   ├── [Entity].java   # Aggregate roots and entities
│   ├── [ValueObject]   # Immutable value objects
│   └── [DomainService] # Domain services (complex business logic)
├── application/         # Use case orchestration
│   ├── command/        # Write operations
│   │   ├── [Command].java
│   │   └── [CommandService].java
│   ├── query/          # Read operations
│   │   ├── [Query].java
│   │   └── [QueryService].java
│   ├── result/         # Application layer DTOs
│   └── event/          # Domain events
├── infrastructure/      # External integrations
│   └── persistence/    # JPA repositories, specifications
├── presentation/        # API layer
│   ├── dto/
│   │   ├── request/    # API request DTOs
│   │   └── response/   # API response DTOs
│   ├── mapper/         # MapStruct presentation mappers
│   └── [Controller].java
└── exception/          # Domain-specific exceptions
```

### Dependency Rules (Critical)
```
presentation → application → domain
infrastructure → domain

Forbidden:
- domain → application
- domain → infrastructure
- domain → presentation
- application → presentation
```

### Entity Layer
**Never use table/column name customization annotations**
```java
// WRONG - Never do this
@Table(name = "users")
@Column(name = "user_name")

// CORRECT - Let JPA naming strategy handle it
@Entity
public class User {
    private String userName;  // Automatically mapped to user_name
}
```

**Rules:**
- Entity class name = table name (User → user, TodoTemplate → todo_template)
- Use @Column only for constraints: nullable, unique, length
- Never use @Column(name = "...")
- Trust Spring's ImprovedNamingStrategy (PascalCase → snake_case)

### Service Layer (CQRS-lite)

**Command Services (Write Operations)**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class TodoCommandService {
    public Long createTodo(CreateTodoCommand command) {
        // Business logic
        // Return ID or Result DTO
    }
}
```

**Query Services (Read Operations)**
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoQueryService {
    public TodoResult getTodo(TodoQuery query) {
        // Fetch and map to Result DTO
    }
}
```

**Rules:**
- CommandService: @Transactional (read-write)
- QueryService: @Transactional(readOnly = true)
- Return domain objects or application Result DTOs
- Throw domain exceptions (handled by GlobalExceptionHandler)

### Repository Layer
```java
public interface TodoRepository extends JpaRepository<TodoTemplate, Long> {
    // Query methods
    List<TodoTemplate> findByUserIdAndDate(UUID userId, LocalDate date);
}

// For dynamic queries
public interface TodoRepositoryCustom {
    Page<TodoTemplate> search(TodoSearchSpecification spec, Pageable pageable);
}
```

**Rules:**
- Extend JpaRepository<Entity, ID>
- Use Specification pattern for complex dynamic queries
- Use @Query with JPQL sparingly (prefer query methods)
- Return domain entities only

### Controller Layer
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/todos")
@Tag(name = "TODO Management")
public class TodoController {
    private final TodoCommandService commandService;
    private final TodoQueryService queryService;
    private final TodoPresentationMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public TodoResponse create(@Valid CreateTodoRequest request,
                                @AuthenticationPrincipal User user) {
        var command = mapper.toCommand(request, UUID.fromString(user.getUsername()));
        var result = commandService.createTodo(command);
        return mapper.toResponse(result);
    }
}
```

**HTTP Status Code Standards:**
- 200 OK: Successful GET, PATCH, PUT with response
- 201 Created: Successful POST with created resource
- 204 No Content: Successful DELETE or update without response
- 400 Bad Request: Validation failure
- 401 Unauthorized: Authentication failure
- 403 Forbidden: Authorization failure
- 404 Not Found: Resource not found

### DTO & Mapping

**Critical Rule: Never manually create DTOs**
```java
// FORBIDDEN - Never use new keyword for DTOs
return new TodoResponse(todo.getId(), todo.getTitle());

// CORRECT - Always use MapStruct
return todoPresentationMapper.toResponse(todo);
```

**DTO Types:**
1. Request DTOs (`presentation/dto/request/`) - API input, Spring binds automatically
2. Response DTOs (`presentation/dto/response/`) - API output, created via MapStruct
3. Command/Query objects (`application/command`, `application/query`) - Use case inputs
4. Result DTOs (`application/result/`) - Use case outputs

**MapStruct Mappers:**
```java
@Mapper(componentModel = "spring")
public interface TodoPresentationMapper {
    // Request → Command
    CreateTodoCommand toCommand(CreateTodoRequest request, UUID userId);

    // Result → Response
    TodoResponse toResponse(TodoResult result);

    // Entity → Response (when needed)
    @Mapping(target = "ownerName", source = "user.nickname")
    TodoResponse toResponse(TodoTemplate entity);
}
```

**Builder Pattern:**
- Production code: Forbidden for DTOs
- Test code: Allowed for test data setup

### Constructor Access Control (Critical)

**Principle: Minimize constructor access to enforce builder pattern and compile-time safety**

```java
// CORRECT - Private/protected constructor with @Builder
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA requirement
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder only
public class TodoTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private LocalDate date;

    // Factory method for complex creation logic (optional)
    public static TodoTemplate create(String title, LocalDate date, User user) {
        return TodoTemplate.builder()
            .title(title)
            .date(date)
            .build();
    }
}

// WRONG - Public constructor allows unsafe instantiation
@Entity
@Getter
@AllArgsConstructor  // Public by default - FORBIDDEN
public class TodoTemplate {
    // ...
}
```

**Rules:**
1. **Entities**:
   - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - Required by JPA, prevents direct instantiation
   - `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Used only by @Builder
   - `@Builder` - Primary construction method
   - Optional: Static factory methods for complex creation logic

2. **DTOs (Request/Response)**:
   - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - Allows framework binding
   - `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Builder only
   - `@Builder` - For test data creation only

3. **Value Objects**:
   - `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Immutable, builder only
   - `@Builder` - Primary construction method
   - Static factory methods preferred for validation

4. **Commands/Queries**:
   - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - MapStruct compatibility
   - `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Builder only
   - `@Builder` - For test and MapStruct

**Benefits:**
- Compile-time enforcement: Cannot accidentally use `new TodoTemplate(...)`
- Clear intent: Builder pattern is the only way
- Validation centralization: Factory methods can enforce invariants
- JPA compatibility: Protected no-args constructor satisfies JPA requirements
- Test clarity: Explicit builder usage in tests

**Example - Factory Method with Validation:**
```java
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Challenge {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    public static Challenge create(String name, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidChallengePeriodException("End date must be after start date");
        }

        return Challenge.builder()
            .name(name)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }
}

// Usage in service
Challenge challenge = Challenge.create(name, start, end);  // ✅ Safe with validation
Challenge invalid = new Challenge(...);  // ❌ Compile error - constructor is private
```

### Validation
```java
// Request DTO with validation
public record CreateTodoRequest(
    @NotBlank @Size(max = 200) String title,
    @SafeHtml String description,  // Custom validator (OWASP sanitizer)
    @Future LocalDate date,
    @Valid RecurrenceRule recurrenceRule
) {}
```

**Available Custom Validators:**
- @SafeHtml - XSS prevention via OWASP sanitizer
- @UniqueEmail - Email uniqueness check
- @ValidPageable - Pageable parameter validation

### Error Handling
```java
// Domain exception
public class TodoNotFoundException extends RuntimeException {
    public TodoNotFoundException(Long id) {
        super("Todo not found: " + id);
    }
}

// GlobalExceptionHandler maps to RFC 7807 Problem Details automatically
```

### Testing Patterns

**Controller Tests (Slice Tests)**
```java
@WebMvcTest(TodoController.class)
@Import({SecurityConfig.class, JwtConfig.class})
class TodoControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean TodoCommandService commandService;
    @MockBean TodoQueryService queryService;

    @Test
    @WithMockUser(username = "user-id", roles = "USER")
    void createTodo_Success() throws Exception {
        // Test with MockMvc
    }
}
```

**Rules:**
- Use @WebMvcTest for controllers (not @SpringBootTest)
- Mock all service dependencies with @MockBean
- Use @WithMockUser for authentication
- Test data: UUID `ffffffff-ffff-ffff-ffff-ffffffffffff` for anonymous user
- Tests must be independent (no shared state)

### JWT Authentication
- RSA key pair for signing (keys in resources/)
- HTTP-Only cookies: `access-token`, `refresh-token`
- Redis for token blacklisting
- MultiBearerTokenResolver supports both Cookie and Bearer header

**Dev Token (100-year expiry):**
See SECURITY_NOTES.md for development token.

### Dynamic Queries
```java
// Specification pattern for type-safe queries
public class TodoSpecification {
    public static Specification<TodoTemplate> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<TodoTemplate> isCompleted(Boolean completed) {
        return (root, query, cb) -> cb.equal(root.get("completed"), completed);
    }
}

// Usage
todoRepository.findAll(
    Specification.where(hasUserId(userId))
        .and(isCompleted(true)),
    pageable
);
```

## Naming Conventions

### Files & Classes
- Entity: `User`, `TodoTemplate`, `Challenge`
- Request DTO: `CreateTodoRequest`, `UpdateUserRequest`
- Response DTO: `TodoResponse`, `ChallengeDetailResponse`
- Command: `CreateTodoCommand`, `UpdateUserCommand`
- Query: `TodoListQuery`, `ChallengeDetailQuery`
- Result: `TodoResult`, `UserResult`
- Service: `TodoCommandService`, `TodoQueryService`, `TodoSearchService`
- Repository: `TodoRepository`, `UserRepository`
- Controller: `TodoController`, `ChallengeController`
- Mapper: `TodoPresentationMapper`, `TodoApplicationMapper`

### Methods
- Command methods: `create`, `update`, `delete`, `activate`
- Query methods: `get`, `find`, `search`, `list`
- Repository methods: `findBy...`, `existsBy...`, `countBy...`

## Lombok & Annotation Standards

### Default Field Access Control

**Principle: All fields must be private by default using lombok.config**

Create `lombok.config` in project root:
```properties
# Enforce private fields by default
lombok.fieldDefaults.defaultPrivate = true
lombok.fieldDefaults.defaultFinal = true

# Enforce access level for constructors
lombok.noArgsConstructor.extraPrivate = false

# Enforce builder usage
lombok.builder.className = Builder
```

**Effect**: All fields without explicit access modifier become `private final`

```java
// With lombok.config
@Getter
@Entity
public class User {
    UUID id;              // Becomes: private final UUID id;
    String email;         // Becomes: private final String email;
    String nickname;      // Becomes: private final String nickname;
}

// Equivalent without lombok.config
@Getter
@Entity
public class User {
    private final UUID id;
    private final String email;
    private final String nickname;
}
```

### Lombok Annotations by Layer

#### 1. Entity Classes (domain/)

**Standard Template:**
```java
@Entity
@Getter                                              // Read access only, no setters
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA requirement, prevent direct instantiation
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder only
@Builder                                             // Primary construction method
@EqualsAndHashCode(onlyExplicitlyIncluded = true)   // Explicit fields only (usually @Id)
@ToString(onlyExplicitlyIncluded = true)            // Explicit fields only (no lazy collections)
public class TodoTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    Long id;                              // private final via lombok.config

    @Column(nullable = false, length = 200)
    String title;                         // private final via lombok.config

    LocalDate date;                       // private final via lombok.config

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;                            // private final via lombok.config

    // Business methods
    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }
}
```

**Rules:**
- `@Getter` only (never `@Setter` - entities are immutable after creation)
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - JPA requirement
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Builder only
- `@Builder` - Primary construction method
- `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` - Prevent N+1 issues with lazy collections
- `@ToString(onlyExplicitlyIncluded = true)` - Prevent N+1 issues with lazy collections
- Business methods for state changes (not setters)

#### 2. Value Objects (domain/)

**Standard Template:**
```java
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode
@ToString
public class RecurrenceRule {
    Frequency frequency;          // private final via lombok.config
    Integer interval;             // private final via lombok.config
    List<DayOfWeek> byWeekDays;  // private final via lombok.config

    public static RecurrenceRule weekly(DayOfWeek... days) {
        return RecurrenceRule.builder()
            .frequency(Frequency.WEEKLY)
            .interval(1)
            .byWeekDays(List.of(days))
            .build();
    }
}
```

**Rules:**
- Immutable: all fields `private final` (via lombok.config)
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Builder only
- `@Builder` - Primary construction
- `@EqualsAndHashCode` and `@ToString` without restrictions (value objects)
- Static factory methods for common patterns

#### 3. Service Classes (application/)

**Standard Template:**
```java
@Service
@RequiredArgsConstructor  // Constructor injection for final fields
@Transactional           // CommandService: read-write
@Slf4j                   // Logging
public class TodoCommandService {
    UserRepository userRepository;           // private final via lombok.config
    TodoRepository todoRepository;           // private final via lombok.config
    ApplicationEventPublisher eventPublisher; // private final via lombok.config
    TodoApplicationMapper mapper;            // private final via lombok.config

    public Long createTodo(CreateTodoCommand command) {
        log.debug("Creating todo for user: {}", command.userId());
        // Business logic
    }
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // QueryService: read-only
@Slf4j
public class TodoQueryService {
    TodoRepository todoRepository;  // private final via lombok.config
    TodoApplicationMapper mapper;   // private final via lombok.config

    public TodoResult getTodo(TodoQuery query) {
        log.debug("Fetching todo: {}", query.id());
        // Query logic
    }
}
```

**Rules:**
- `@RequiredArgsConstructor` - Constructor injection for all final fields
- `@Transactional` for CommandService, `@Transactional(readOnly = true)` for QueryService
- `@Slf4j` - Logging (prefer over manual logger declaration)
- All dependencies as `final` fields (injected via constructor)

#### 4. Controllers (presentation/)

**Standard Template:**
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos")
@Tag(name = "TODO Management", description = "Personal TODO CRUD operations")
@Validated
public class TodoController {
    TodoCommandService commandService;        // private final via lombok.config
    TodoQueryService queryService;            // private final via lombok.config
    TodoPresentationMapper mapper;            // private final via lombok.config

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create TODO", description = "Creates new TODO with optional recurrence")
    public TodoResponse create(@Valid CreateTodoRequest request,
                                @AuthenticationPrincipal User user) {
        // Controller logic
    }
}
```

**Rules:**
- `@RestController` (not `@Controller` + `@ResponseBody`)
- `@RequiredArgsConstructor` - Constructor injection
- `@RequestMapping` with API version prefix `/api/v1/`
- `@Tag` for Swagger documentation
- `@Validated` for method-level validation
- All dependencies as `final` fields

#### 5. DTOs (presentation/dto/, application/)

**Request/Response DTOs (prefer records):**
```java
// Request DTO (record)
public record CreateTodoRequest(
    @NotBlank @Size(max = 200) String title,
    @SafeHtml String description,
    @Future LocalDate date,
    @Valid RecurrenceRuleRequest recurrenceRule
) {}

// Response DTO (record)
public record TodoResponse(
    Long id,
    String title,
    LocalDate date,
    Boolean completed
) {}

// Complex DTO (class with builder)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // MapStruct/Jackson
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Builder only
@Builder
public class TodoDetailResponse {
    Long id;                    // private final via lombok.config
    String title;               // private final via lombok.config
    LocalDate date;             // private final via lombok.config
    UserSummary user;           // private final via lombok.config
    CategorySummary category;   // private final via lombok.config
}
```

**Rules:**
- Prefer Java records for simple DTOs
- For complex DTOs: use class with Lombok
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` - Framework binding (Jackson, Spring)
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` - Builder only
- `@Builder` for test data and optional MapStruct usage
- Never `@Setter` on DTOs

#### 6. MapStruct Mappers (presentation/mapper/, application/mapper/)

**Standard Template:**
```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,  // Fail on unmapped fields
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TodoPresentationMapper {
    // Request → Command
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "title", source = "request.title")
    CreateTodoCommand toCommand(CreateTodoRequest request, UUID userId);

    // Entity → Response
    @Mapping(target = "ownerName", source = "user.nickname")
    @Mapping(target = "categoryName", source = "category.name")
    TodoResponse toResponse(TodoTemplate entity);

    // Result → Response
    TodoResponse toResponse(TodoResult result);
}
```

**Rules:**
- `componentModel = "spring"` - Spring Bean
- `unmappedTargetPolicy = ReportingPolicy.ERROR` - Compile-time safety
- Explicit `@Mapping` for all non-trivial mappings
- Never manual DTO creation in mapper implementation

### JPA Annotations Standards

#### Entity Annotations
```java
@Entity                                    // Required, no @Table
@Getter                                    // Lombok
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TodoTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment for Long ID
    Long id;

    @Column(nullable = false, length = 200)  // Constraints only, NO name customization
    String title;

    @Enumerated(EnumType.STRING)  // Always STRING, never ORDINAL
    Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)  // Always LAZY by default
    @JoinColumn(name = "user_id", nullable = false)  // FK column name allowed
    User user;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<TodoTag> tags = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
```

**Critical Rules:**
1. **NO @Table annotation** - Let naming strategy handle table names
2. **NO @Column(name = "...")** - Let naming strategy handle column names
3. **@Column for constraints only**: `nullable`, `unique`, `length`, `precision`, `scale`
4. **@Enumerated(EnumType.STRING)** - Always STRING for enums
5. **@ManyToOne/@OneToOne default to LAZY** - Explicit is better
6. **@OneToMany/@ManyToMany must specify mappedBy or @JoinTable**
7. **@JoinColumn(name = "...")** - FK column names are allowed for clarity
8. **@Builder.Default** - For collections in builder pattern

### Validation Annotations Standards

**Bean Validation:**
```java
public record CreateTodoRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be 1-200 characters")
    String title,

    @SafeHtml  // Custom validator
    @Size(max = 2000, message = "Description too long")
    String description,

    @NotNull
    @Future(message = "Date must be in the future")
    LocalDate date,

    @Valid  // Cascade validation
    RecurrenceRuleRequest recurrenceRule,

    @Min(1) @Max(5)
    Integer priorityId
) {}
```

**Available Validators:**
- Standard: `@NotNull`, `@NotBlank`, `@NotEmpty`, `@Size`, `@Min`, `@Max`, `@Future`, `@Past`, `@Email`
- Custom: `@SafeHtml`, `@UniqueEmail`, `@ValidPageable`, `@ValidRecurrenceRule`

### Spring Annotations Standards

**Dependency Injection:**
```java
// CORRECT - Constructor injection with @RequiredArgsConstructor
@Service
@RequiredArgsConstructor
public class UserService {
    UserRepository userRepository;        // private final via lombok.config
    PasswordEncoder passwordEncoder;      // private final via lombok.config
}

// WRONG - Field injection
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // FORBIDDEN
}
```

**Transaction Management:**
```java
// CORRECT - Class-level for consistency
@Service
@RequiredArgsConstructor
@Transactional  // All methods are transactional
public class TodoCommandService {
    // Methods
}

// CORRECT - Read-only optimization
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoQueryService {
    // Query methods
}

// WRONG - Method-level transactions (inconsistent)
@Service
public class TodoService {
    @Transactional  // Scattered transaction management
    public void create() {}
}
```

**Security Annotations:**
```java
@PreAuthorize("hasRole('USER')")                    // Role check
@PreAuthorize("hasAuthority('TODO_WRITE')")        // Permission check
@PreAuthorize("#userId == authentication.name")     // Owner check
@PreAuthorize("@securityService.canAccess(#id)")   // Custom logic
```

### Annotation Order Standards

**Consistent ordering improves readability:**

```java
// Class-level annotations order
@Entity                          // 1. JPA/persistence
@Getter                          // 2. Lombok data
@NoArgsConstructor(...)          // 3. Lombok constructors
@AllArgsConstructor(...)
@Builder                         // 4. Lombok builder
@EqualsAndHashCode(...)          // 5. Lombok equals/hashCode
@ToString(...)                   // 6. Lombok toString
@Service                         // 7. Spring stereotype
@RestController
@RequiredArgsConstructor         // 8. Spring injection
@Transactional                   // 9. Spring transaction
@RequestMapping(...)             // 10. Spring web
@Tag(...)                        // 11. Swagger
@Validated                       // 12. Validation

// Field-level annotations order
@Id                              // 1. JPA identity
@GeneratedValue(...)             // 2. JPA generation
@Column(...)                     // 3. JPA column
@Enumerated(...)                 // 4. JPA enum
@ManyToOne(...)                  // 5. JPA relationship
@JoinColumn(...)                 // 6. JPA join
@NotNull                         // 7. Validation
@Size(...)                       // 8. Validation
@EqualsAndHashCode.Include       // 9. Lombok equals/hash
@ToString.Include                // 10. Lombok toString
@Builder.Default                 // 11. Lombok builder

// Method-level annotations order
@PostMapping(...)                // 1. HTTP mapping
@ResponseStatus(...)             // 2. HTTP status
@PreAuthorize(...)               // 3. Security
@Operation(...)                  // 4. Swagger operation
@ApiResponse(...)                // 5. Swagger response
@Transactional                   // 6. Transaction (if method-level)
@Cacheable(...)                  // 7. Caching
```

## Code Style

### Modern Java
```java
// Pattern matching
if (obj instanceof User user) {
    return user.getEmail();
}

// Switch expressions
return switch (status) {
    case ACTIVE -> "Active user";
    case INACTIVE -> "Inactive user";
    default -> "Unknown status";
};

// Text blocks
String query = """
    SELECT u FROM User u
    WHERE u.email = :email
    AND u.active = true
    """;

// Records for DTOs
public record CreateUserCommand(String email, String password) {}
```

## Common Pitfalls (Avoid)

1. Never use `new` for DTO creation in production code
2. Never violate layer dependencies
3. Never use @Table or @Column(name = "...")
4. Never write tests with shared mutable state
5. Never hardcode values (use application properties)
6. Never log sensitive data (passwords, tokens)
7. Never catch generic Exception (catch specific exceptions)
8. Never return null (use Optional)

## Performance Considerations

### N+1 Query Prevention
```java
// Use @EntityGraph or JOIN FETCH
@EntityGraph(attributePaths = {"user", "category"})
List<TodoTemplate> findAllByUserId(UUID userId);

// Or explicit JOIN FETCH in JPQL
@Query("SELECT t FROM TodoTemplate t JOIN FETCH t.user WHERE t.id = :id")
Optional<TodoTemplate> findByIdWithUser(@Param("id") Long id);
```

### Pagination
- Always use Pageable for list endpoints
- Default page size: 20
- Maximum page size: 100
- Sort fields validation via @ValidPageable

## Security Rules

1. All endpoints require authentication (except public ones explicitly marked)
2. Use @PreAuthorize for method-level security
3. Sanitize HTML input with @SafeHtml
4. Never trust client input (always validate)
5. Use parameterized queries (JPA handles this)
6. HTTP-Only cookies for tokens (XSS prevention)
7. CORS configured per environment

## Documentation Standards

### Swagger Annotations
```java
@Operation(
    summary = "Create TODO",
    description = "Creates a new TODO item with optional recurrence schedule"
)
@ApiResponse(responseCode = "201", description = "TODO created successfully")
@ApiResponse(responseCode = "400", description = "Invalid input")
@PostMapping
public TodoResponse create(@Valid CreateTodoRequest request) {
    // ...
}
```

## Development Workflow

1. Read domain requirements
2. Design domain model (entities, value objects)
3. Write domain tests
4. Implement domain logic
5. Create application services (command/query)
6. Write application service tests
7. Create presentation DTOs and mappers
8. Implement controllers
9. Write controller tests
10. Update Swagger documentation
11. Manual QA via Swagger UI

## Quick Reference

### Database Access
```
PostgreSQL: localhost:5432/ttodo_dev (ttodo_user/ttodo_password)
Redis: localhost:6379
```

### Endpoints
```
Swagger UI: http://localhost:8080/swagger-ui/index.html
API Docs: http://localhost:8080/v3/api-docs
Health: http://localhost:8080/actuator/health
```

### Test Execution
```bash
./gradlew test                          # All tests
./gradlew test --tests "*ControllerTest"  # Controller tests
./gradlew test --tests "TodoServiceTest"  # Specific test
```

## Version Information
- Java: 21 LTS
- Spring Boot: 3.5.6
- Spring Framework: 6.2.x
- PostgreSQL: 17
- Redis: 7

Last Updated: 2025-10-02
