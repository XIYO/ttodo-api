# ZZIC-API 프로젝트 문서

## 📋 프로젝트 개요

ZZIC-API는 Todo 관리 시스템을 위한 REST API 서버입니다. Spring Boot 3.2.5와 Java 21을 기반으로 구축되었으며, JWT 인증과 함께 회원 관리 및 Todo CRUD 기능을 제공합니다.

## 🏗️ 아키텍처 구조

### 레이어드 아키텍처 (Layered Architecture)
프로젝트는 계층형 아키텍처 패턴을 따르며, 다음과 같은 계층으로 구성됩니다:

```
┌─────────────────────────────────┐
│     Presentation Layer          │  ← Controller, DTO
├─────────────────────────────────┤
│     Application Layer           │  ← Service, Command/Query
├─────────────────────────────────┤
│     Domain Layer                │  ← Entity, Domain Logic
├─────────────────────────────────┤
│     Infrastructure Layer        │  ← Repository, External APIs
└─────────────────────────────────┘
```

## 📁 프로젝트 구조

```
src/main/java/point/zzicback/
├── ZzicBackApplication.java          # 메인 애플리케이션 클래스
├── common/                           # 공통 컴포넌트
│   ├── config/                       # 설정 클래스들
│   │   ├── AppConfig.java
│   │   ├── CorsConfig.java
│   │   ├── JwtConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── error/                        # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   └── EntityNotFoundException.java
│   ├── jwt/                          # JWT 관련 유틸리티
│   ├── properties/                   # 설정 프로퍼티
│   ├── security/                     # 보안 관련 컴포넌트
│   ├── utill/                        # 유틸리티 클래스
│   └── validation/                   # 커스텀 밸리데이션
├── member/                           # 회원 도메인
│   ├── application/                  # 애플리케이션 서비스
│   │   ├── MemberService.java
│   │   ├── dto/
│   │   │   ├── command/             # 명령 객체
│   │   │   │   ├── SignUpCommand.java
│   │   │   │   └── SignInCommand.java
│   │   │   ├── query/               # 조회 객체
│   │   │   │   └── MemberQuery.java
│   │   │   └── response/            # 응답 DTO
│   │   │       └── MemberMeResponse.java
│   │   └── mapper/
│   │       └── MemberApplicationMapper.java
│   ├── config/                       # 회원 관련 설정
│   ├── domain/                       # 도메인 엔티티
│   │   ├── Member.java               # 회원 엔티티
│   │   └── AuthenticatedMember.java  # 인증된 회원 DTO
│   ├── persistance/                  # 데이터 접근 계층
│   │   └── MemberRepository.java
│   └── presentation/                 # 프레젠테이션 계층
│       ├── AuthController.java
│       ├── MemberController.java
│       ├── dto/                     # 컨트롤러용 DTO
│       │   ├── SignUpRequest.java
│       │   ├── SignInRequest.java
│       │   ├── MemberMeResponse.java
│       │   ├── PasswordOrAnonymousValid.java
│       │   └── PasswordOrAnonymousValidator.java
│       └── mapper/
│           └── MemberPresentationMapper.java
└── todo/                            # Todo 도메인
    ├── application/                 # 애플리케이션 서비스
    │   ├── TodoService.java
    │   ├── dto/
    │   │   ├── command/             # 명령 객체
    │   │   │   ├── CreateTodoCommand.java
    │   │   │   └── UpdateTodoCommand.java
    │   │   ├── query/               # 조회 객체
    │   │   │   ├── TodoQuery.java
    │   │   │   └── TodoListQuery.java
    │   │   └── response/            # 응답 DTO
    │   │       └── TodoResponse.java
    │   └── mapper/
    │       └── TodoApplicationMapper.java
    ├── domain/                      # 도메인 엔티티
    │   └── Todo.java
    ├── persistance/                 # 데이터 접근 계층
    │   └── TodoRepository.java
    └── presentation/                # 프레젠테이션 계층
        ├── TodoController.java
        ├── dto/                     # 컨트롤러용 DTO
        │   ├── CreateTodoRequest.java
        │   ├── UpdateTodoRequest.java
        │   └── TodoResponse.java
        └── mapper/
            └── TodoPresentationMapper.java
```

## 🛠️ 사용 기술 스택

### 핵심 프레임워크
- **Spring Boot 3.2.5** - 메인 프레임워크
- **Java 21** - 프로그래밍 언어
- **Gradle** - 빌드 도구

### 데이터베이스
- **Spring Data JPA** - ORM 프레임워크
- **H2 Database** - 개발/테스트용 인메모리 데이터베이스
- **PostgreSQL** - 운영환경 데이터베이스
- **Redis** - 캐싱 및 세션 관리

### 보안
- **Spring Security** - 보안 프레임워크
- **OAuth2 Resource Server** - JWT 토큰 기반 인증
- **JWT (JSON Web Token)** - 인증 토큰

### 개발 도구
- **Lombok** - 보일러플레이트 코드 제거
- **MapStruct** - 객체 매핑 라이브러리
- **Swagger/OpenAPI 3** - API 문서화
- **Spring Boot Validation** - 입력값 검증

### 기타
- **Thymeleaf** - 서버사이드 템플릿 엔진
- **Docker** - 컨테이너화

## 📐 코딩 컨벤션

### 패키지 구조 컨벤션
- **도메인 기반 패키지 구조**: 각 도메인(member, todo)별로 패키지 분리
- **계층별 하위 패키지**: application, domain, persistence, presentation

### 네이밍 컨벤션
- **클래스**: PascalCase (예: `TodoService`, `MemberRepository`)
- **메서드/변수**: camelCase (예: `createTodo`, `memberId`)
- **상수**: UPPER_SNAKE_CASE
- **패키지**: 소문자 + 언더스코어 (예: `persistence`)

### DTO 네이밍 패턴
- **Request**: `~Request` (예: `CreateTodoRequest`)
- **Response**: `~Response` (예: `TodoResponse`)
- **Command**: `~Command` (예: `CreateTodoCommand`)
- **Query**: `~Query` (예: `TodoListQuery`)

### 어노테이션 사용
- `@RestController` - REST 컨트롤러
- `@Service` - 서비스 클래스
- `@Repository` - 레포지토리 인터페이스
- `@Entity` - JPA 엔티티
- `@Transactional` - 트랜잭션 관리

## 🗄️ 데이터베이스 설계

### Member 엔티티
```sql
Members
├── id (UUID, PK)
├── email (String, Unique)
├── nickname (String)
└── password (String)
```

### Todo 엔티티
```sql
Todos
├── id (Long, PK, Auto-increment)
├── title (String, Not Null)
├── description (String)
├── done (Boolean, Not Null)
└── member_id (UUID, FK → Members.id)
```

## 🔄 API 설계 패턴

### RESTful API 설계
- **리소스 중심 URL**: `/api/members/{memberId}/todos`
- **HTTP 메서드별 역할**:
  - GET: 조회
  - POST: 생성
  - PUT: 전체 수정
  - DELETE: 삭제

### 응답 상태 코드
- **200 OK**: 조회 성공
- **201 Created**: 생성 성공
- **204 No Content**: 수정/삭제 성공
- **400 Bad Request**: 잘못된 요청
- **404 Not Found**: 리소스 없음
- **500 Internal Server Error**: 서버 오류

## 🔒 보안 설계

### JWT 기반 인증
- **Access Token**: 60분 유효
- **Refresh Token**: 1년 유효
- **Cookie 기반**: HttpOnly, Secure 설정

### CORS 설정
- 개발환경에서 localhost 허용
- 운영환경에서 특정 도메인만 허용

## 🧪 테스트 전략

### 테스트 계층
- **Unit Test**: 개별 메서드 테스트
- **Integration Test**: 여러 컴포넌트 통합 테스트
- **API Test**: 전체 API 엔드포인트 테스트

## 📝 설정 파일

### application.yml
```yaml
spring:
  profiles:
    active: db-h2  # 기본 프로파일
  jpa:
    hibernate:
      ddl-auto: create  # 개발용 스키마 자동 생성
```

### 프로파일별 설정
- **db-h2**: 개발용 H2 데이터베이스
- **db-pg**: PostgreSQL 데이터베이스
- **prod**: 운영환경 설정

## 🚀 배포 및 실행

### 로컬 실행
```bash
# Gradle을 통한 실행
./gradlew bootRun

# JAR 파일 생성 및 실행
./gradlew build
java -jar build/libs/zzic-api-0.0.1-SNAPSHOT.jar
```

### Docker 실행
```bash
# Docker 이미지 빌드
docker build -t zzic-api .

# Docker Compose 실행
docker-compose up -d
```

## 📚 API 문서

### Swagger UI
- **로컬 접속**: http://localhost:8080/
- **API 문서**: http://localhost:8080/v3/api-docs

## 🔧 주요 설정 및 유틸리티

### MapStruct 매퍼
- **TodoPresentationMapper**: Controller ↔ Application 계층 매핑
- **TodoApplicationMapper**: Application ↔ Domain 계층 매핑

### 전역 예외 처리
- `GlobalExceptionHandler`: 모든 예외를 중앙에서 처리
- `EntityNotFoundException`: 엔티티 조회 실패 시 404 응답

### 검증 (Validation)
- Bean Validation 사용
- 커스텀 검증: `@UniqueEmail`, `@FieldComparison`

## 📈 성능 최적화

### JPA 최적화
- `@Transactional(readOnly = true)`: 읽기 전용 트랜잭션
- LAZY Loading: 연관관계 지연 로딩

### 페이징 처리
- Spring Data의 `Pageable` 인터페이스 활용
- 정렬 기능 포함

## 🔄 개발 워크플로우

1. **요구사항 분석** → **도메인 모델링**
2. **엔티티 설계** → **레포지토리 구현**
3. **서비스 로직** → **컨트롤러 구현**
4. **테스트 코드** → **API 문서화**

---

이 문서는 ZZIC-API 프로젝트의 전반적인 구조와 사용된 기술, 컨벤션을 설명합니다. 
새로운 개발자가 프로젝트에 참여할 때 이 문서를 참고하여 빠르게 프로젝트를 이해하고 개발에 참여할 수 있습니다.
