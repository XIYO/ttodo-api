# TTODO - 게임화된 할 일 관리 서비스

## 프로젝트 개요

TTODO는 일상의 할 일을 게임처럼 관리할 수 있는 서비스입니다. 사용자는 할 일을 완료하며 경험치를 얻고, 레벨을 올리며, 다른 사용자와 함께 챌린지에 참여할 수 있습니다.

### 핵심 기능
- 📝 **할 일 관리**: 카테고리별 할 일 생성, 반복 일정 설정, 우선순위 및 태그 관리
- 🎮 **게임화 요소**: 경험치 시스템, 레벨업, 성장 그래프
- 🏆 **챌린지 시스템**: 공개/비공개 챌린지 생성 및 참여
- 👤 **프로필 관리**: 테마 커스터마이징, 프로필 이미지 업로드

### 기술 스택
- **Backend**: Spring Boot 3.5.0, Java 21
- **Database**: PostgreSQL, Redis
- **Security**: Spring Security + JWT
- **Architecture**: DDD, 헥사고날 아키텍처
- **API**: RESTful API, SpringDoc OpenAPI
- **Infra**: Docker, GitHub Actions

## 프로젝트 아키텍처

### 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Client]
        MOBILE[Mobile Client]
    end
    
    subgraph "API Gateway"
        NGINX[Nginx<br/>Reverse Proxy]
    end
    
    subgraph "Application Layer"
        SPRING[Spring Boot Application<br/>Port: 8080]
    end
    
    subgraph "Data Layer"
        POSTGRES[(PostgreSQL<br/>Port: 5432)]
        REDIS[(Redis<br/>Port: 6379)]
        S3[AWS S3<br/>Image Storage]
    end
    
    WEB --> NGINX
    MOBILE --> NGINX
    NGINX --> SPRING
    SPRING --> POSTGRES
    SPRING --> REDIS
    SPRING --> S3
```

### DDD 레이어 아키텍처

```mermaid
graph TB
    subgraph "Presentation Layer"
        CONTROLLER[Controller]
        REQ_DTO[Request DTO]
        RES_DTO[Response DTO]
        PRES_MAPPER[Presentation Mapper]
    end
    
    subgraph "Application Layer"
        SERVICE[Service]
        COMMAND[Command DTO]
        QUERY[Query DTO]
        RESULT[Result DTO]
        APP_MAPPER[Application Mapper]
        EVENT_HANDLER[Event Handler]
    end
    
    subgraph "Domain Layer"
        ENTITY[Entity]
        DOMAIN_SERVICE[Domain Service]
        DOMAIN_EVENT[Domain Event]
        VALUE_OBJECT[Value Object]
    end
    
    subgraph "Infrastructure Layer"
        REPOSITORY[Repository]
        EXTERNAL_API[External API]
        FILE_STORAGE[File Storage]
    end
    
    CONTROLLER --> SERVICE
    SERVICE --> ENTITY
    SERVICE --> REPOSITORY
    ENTITY --> DOMAIN_EVENT
    EVENT_HANDLER --> DOMAIN_EVENT
    PRES_MAPPER --> RESULT
    PRES_MAPPER --> RES_DTO
    APP_MAPPER --> ENTITY
    APP_MAPPER --> RESULT
```

## 도메인별 상세 구조

### 1. Member (회원) 도메인

```mermaid
classDiagram
    class Member {
        +Long id
        +String email
        +String nickname
        +String password
        +MemberRole role
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
        +boolean active
    }
    
    class MemberService {
        +MemberResult createMember(SignUpCommand)
        +MemberResult getMember(Long)
        +MemberResult updateMember(UpdateCommand)
        +void deleteMember(Long)
        +boolean checkNicknameDuplicate(String)
    }
    
    class MemberController {
        +ResponseEntity createMember(SignUpRequest)
        +ResponseEntity getMember(Long)
        +ResponseEntity updateMember(UpdateRequest)
        +ResponseEntity deleteMember(Long)
        +ResponseEntity checkNickname(String)
    }
    
    class MemberRepository {
        +Optional findById(Long)
        +Optional findByEmail(String)
        +boolean existsByNickname(String)
        +Member save(Member)
    }
    
    MemberController --> MemberService
    MemberService --> MemberRepository
    MemberService --> Member
    MemberRepository --> Member
```

### 2. Todo 도메인

```mermaid
classDiagram
    class TodoOriginal {
        +Long id
        +Long memberId
        +String title
        +String description
        +LocalDate dueDate
        +LocalTime dueTime
        +boolean allDay
        +RepeatType repeatType
        +Set~DayOfWeek~ daysOfWeek
        +LocalDate repeatEndDate
        +Priority priority
        +Set~String~ tags
        +Long categoryId
        +boolean isDeleted
        +Instant createdAt
        +Instant updatedAt
    }
    
    class TodoService {
        +TodoResult createTodo(CreateTodoCommand)
        +TodoResult updateTodo(UpdateTodoCommand)
        +void deleteTodo(Long, Long)
        +Page getTodos(Long, Pageable)
        +TodoResult getTodo(Long, Long)
    }
    
    class Category {
        +Long id
        +Long memberId
        +String title
        +String color
        +int orderIndex
        +Instant createdAt
        +Instant updatedAt
    }
    
    class CategoryService {
        +CategoryResult createCategory(CreateCommand)
        +CategoryResult updateCategory(UpdateCommand)
        +void deleteCategory(DeleteCommand)
        +List getCategories(Long)
    }
    
    TodoOriginal "N" --> "1" Category : belongsTo
    TodoOriginal "N" --> "1" Member : ownedBy
    Category "N" --> "1" Member : ownedBy
```

### 3. Profile 도메인

```mermaid
classDiagram
    class Profile {
        +Long id
        +Long memberId
        +String theme
        +String introduction
        +String profileImageUrl
        +Instant createdAt
        +Instant updatedAt
    }
    
    class ProfileService {
        +ProfileResult createProfile(CreateCommand)
        +ProfileResult updateProfile(UpdateCommand)
        +void deleteProfile(Long)
        +ProfileResult getProfile(Long)
        +String uploadProfileImage(MultipartFile, Long)
    }
    
    class ProfileController {
        +ResponseEntity createProfile(CreateRequest)
        +ResponseEntity updateProfile(UpdateRequest)
        +ResponseEntity deleteProfile(Long)
        +ResponseEntity getProfile(Long)
        +ResponseEntity uploadImage(MultipartFile, Long)
    }
    
    Profile "1" --> "1" Member : belongsTo
    ProfileController --> ProfileService
    ProfileService --> Profile
```

### 4. Challenge 도메인

```mermaid
classDiagram
    class Challenge {
        +Long id
        +String title
        +String description
        +Long creatorId
        +ChallengeType type
        +String inviteCode
        +LocalDateTime startDate
        +LocalDateTime endDate
        +int maxParticipants
        +Instant createdAt
        +Instant updatedAt
    }
    
    class ChallengeParticipation {
        +Long id
        +Long challengeId
        +Long memberId
        +ParticipationStatus status
        +LocalDateTime joinedAt
    }
    
    class ChallengeTodo {
        +Long id
        +Long challengeId
        +Long memberId
        +String title
        +boolean isCompleted
        +LocalDateTime completedAt
    }
    
    Challenge "1" --> "N" ChallengeParticipation
    Challenge "1" --> "N" ChallengeTodo
    ChallengeParticipation "N" --> "1" Member
    ChallengeTodo "N" --> "1" Member
```

### 5. Experience & Level 도메인

```mermaid
classDiagram
    class MemberExperience {
        +Long id
        +Long memberId
        +int currentExperience
        +int totalExperience
        +int currentLevel
        +LocalDateTime lastActivityDate
        +Instant createdAt
        +Instant updatedAt
    }
    
    class Level {
        +Long id
        +int level
        +int requiredExperience
        +String title
        +String description
    }
    
    class ExperienceService {
        +void addExperience(Long, int)
        +ExperienceResult getExperience(Long)
        +LevelResult checkLevelUp(Long)
    }
    
    MemberExperience "N" --> "1" Member
    ExperienceService --> MemberExperience
    ExperienceService --> Level
```

## 주요 기능 플로우

### 1. 회원가입 및 로그인 플로우

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant MemberService
    participant JwtProvider
    participant Redis
    participant Database
    
    Client->>AuthController: POST /auth/signup
    AuthController->>MemberService: createMember()
    MemberService->>Database: save(Member)
    Database-->>MemberService: Member
    MemberService-->>AuthController: MemberResult
    AuthController-->>Client: 201 Created
    
    Client->>AuthController: POST /auth/login
    AuthController->>AuthService: login()
    AuthService->>Database: findByEmail()
    AuthService->>AuthService: validatePassword()
    AuthService->>JwtProvider: generateTokens()
    JwtProvider-->>AuthService: TokenPair
    AuthService->>Redis: saveRefreshToken()
    AuthService-->>AuthController: TokenResponse
    AuthController-->>Client: 200 OK + Tokens
```

### 2. Todo 생성 및 경험치 획득 플로우

```mermaid
sequenceDiagram
    participant Client
    participant TodoController
    participant TodoService
    participant CategoryService
    participant EventPublisher
    participant ExperienceHandler
    participant ExperienceService
    participant Database
    
    Client->>TodoController: POST /todos
    TodoController->>TodoService: createTodo()
    TodoService->>CategoryService: validateCategory()
    TodoService->>Database: save(Todo)
    TodoService->>EventPublisher: publish(TodoCreatedEvent)
    EventPublisher->>ExperienceHandler: handle(TodoCreatedEvent)
    ExperienceHandler->>ExperienceService: addExperience()
    ExperienceService->>Database: updateExperience()
    TodoService-->>TodoController: TodoResult
    TodoController-->>Client: 201 Created
```

## 보안 및 인증

### JWT 토큰 기반 인증

```mermaid
graph LR
    subgraph "Client"
        REQ[Request with Token]
    end
    
    subgraph "Security Filter Chain"
        JWT_FILTER[JwtAuthenticationFilter]
        AUTH_FILTER[AuthenticationFilter]
    end
    
    subgraph "Token Validation"
        JWT_PROVIDER[JwtProvider]
        REDIS_CHECK[Redis Token Check]
    end
    
    subgraph "Controller"
        SECURED[Secured Endpoint]
    end
    
    REQ --> JWT_FILTER
    JWT_FILTER --> JWT_PROVIDER
    JWT_PROVIDER --> REDIS_CHECK
    REDIS_CHECK --> AUTH_FILTER
    AUTH_FILTER --> SECURED
```

### 주요 보안 기능
- **JWT 토큰**: Access Token (30분), Refresh Token (7일)
- **Redis 세션 관리**: 토큰 블랙리스트, 리프레시 토큰 저장
- **비밀번호 암호화**: BCrypt
- **CORS 설정**: 프론트엔드 도메인 허용
- **Rate Limiting**: API 요청 제한 (준비 중)

## 성능 최적화

### 1. 데이터베이스 최적화
- **인덱스 설계**: 자주 조회되는 컬럼에 인덱스 추가
- **N+1 문제 해결**: Fetch Join, @EntityGraph 사용
- **페이징 처리**: Spring Data Pageable

### 2. 캐싱 전략
```mermaid
graph TB
    subgraph "Cache Layer"
        REDIS_CACHE[Redis Cache]
        LOCAL_CACHE[Local Cache<br/>@Cacheable]
    end
    
    subgraph "Data Flow"
        REQUEST[API Request]
        CACHE_CHECK{Cache Hit?}
        DATABASE[(Database)]
        RESPONSE[API Response]
    end
    
    REQUEST --> CACHE_CHECK
    CACHE_CHECK -->|Hit| REDIS_CACHE
    CACHE_CHECK -->|Miss| DATABASE
    DATABASE --> REDIS_CACHE
    REDIS_CACHE --> RESPONSE
```

### 3. 비동기 처리
- **이벤트 기반 아키텍처**: Spring Events
- **비동기 로깅**: Log4j2 Async Appender
- **파일 업로드**: 비동기 S3 업로드

## 배포 및 인프라

### Docker Compose 구성

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - postgres
      - redis
  
  postgres:
    image: postgres:15
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7
    ports:
      - "6379:6379"
```

### CI/CD 파이프라인

```mermaid
graph LR
    subgraph "Development"
        CODE[Code Push]
        PR[Pull Request]
    end
    
    subgraph "GitHub Actions"
        BUILD[Build & Test]
        DOCKER[Docker Build]
        DEPLOY[Deploy]
    end
    
    subgraph "Production"
        SERVER[Production Server]
        HEALTH[Health Check]
    end
    
    CODE --> PR
    PR --> BUILD
    BUILD --> DOCKER
    DOCKER --> DEPLOY
    DEPLOY --> SERVER
    SERVER --> HEALTH
```

## 모니터링 및 로깅

### 로깅 전략
- **구조화된 로깅**: JSON 형식
- **로그 레벨**: ERROR, WARN, INFO, DEBUG
- **비동기 파일 로깅**: 성능 최적화
- **일별 로그 롤링**: 30일 보관

### 모니터링 (계획)
- **APM**: Application Performance Monitoring
- **메트릭 수집**: Prometheus + Grafana
- **알림**: Slack Integration

## 향후 개선 계획

### 1. 기능 확장
- [ ] 소셜 기능 강화 (친구 추가, 피드)
- [ ] AI 기반 할 일 추천
- [ ] 음성 인식 할 일 등록
- [ ] 위젯 지원

### 2. 기술적 개선
- [ ] GraphQL API 추가
- [ ] 마이크로서비스 전환
- [ ] Kubernetes 배포
- [ ] 실시간 알림 (WebSocket)

### 3. 성능 개선
- [ ] 데이터베이스 샤딩
- [ ] CDN 적용
- [ ] 검색 엔진 도입 (Elasticsearch)

## 프로젝트 성과

### 기술적 성과
- **DDD 아키텍처 구현**: 도메인 중심 설계로 유지보수성 향상
- **이벤트 기반 설계**: 도메인 간 느슨한 결합
- **테스트 커버리지**: 단위 테스트, 통합 테스트 구현
- **API 문서화**: Swagger UI 제공

### 학습 포인트
- Spring Boot 3.x와 Java 21의 최신 기능 활용
- DDD와 헥사고날 아키텍처 실전 적용
- Docker와 CI/CD를 통한 자동화 구축
- 성능 최적화와 모니터링 경험

## 프로젝트 링크
- **GitHub**: [https://github.com/GET-to-the-POINT/ttodo-api](https://github.com/GET-to-the-POINT/ttodo-api)
- **API Documentation**: [배포 후 제공 예정]
- **Demo**: [준비 중]