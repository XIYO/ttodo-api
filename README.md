# ZZIC-BACK

ZZIC의 백엔드 API 서버입니다. 개인 TODO 관리와 챌린지를 통한 동기부여 기능을 제공합니다.

## 기술 스택

### Backend
- **Java 24** - 최신 Java 기능 활용
- **Spring Boot 3.5.0** - 백엔드 프레임워크
- **Spring Security** - 인증/인가 처리
- **Spring Data JPA** - ORM 및 데이터베이스 연동
- **Spring Data Redis** - 토큰 저장소 및 캐싱
- **Spring Validation** - 입력 데이터 검증

### Database
- **PostgreSQL** - 운영 데이터베이스
- **H2** - 개발/테스트 데이터베이스
- **Redis** - 토큰 저장소 및 세션 관리

### Documentation & Testing
- **Swagger/OpenAPI 3** - API 문서화
- **JUnit 5** - 단위 테스트
- **Spring Boot Test** - 통합 테스트

### Libraries
- **Lombok** - 보일러플레이트 코드 제거
- **MapStruct** - 객체 매핑
- **JWT** - 토큰 기반 인증

### DevOps
- **Docker** - 컨테이너화
- **Docker Compose** - 로컬 개발 환경
- **GitHub Actions** - CI/CD
- **GitHub Container Registry** - 이미지 저장소

## 프로젝트 구조

```
src/
├── main/java/point/zzicback/
│   ├── auth/                    # 인증/인가 관련
│   │   ├── application/         # 토큰 서비스
│   │   ├── config/              # 인증 설정
│   │   ├── domain/              # 인증 도메인
│   │   ├── infrastructure/      # 토큰 저장소
│   │   ├── presentation/        # 인증 API
│   │   └── security/            # 보안 설정
│   ├── member/                  # 회원 관리
│   │   ├── application/         # 회원 서비스
│   │   ├── domain/              # 회원 도메인
│   │   └── infrastructure/      # 회원 데이터 저장소
│   ├── todo/                    # 개인 TODO 관리
│   │   ├── application/         # TODO 서비스
│   │   ├── domain/              # TODO 도메인
│   │   ├── infrastructure/      # TODO 데이터 저장소
│   │   └── presentation/        # TODO API
│   ├── challenge/               # 챌린지 관리
│   │   ├── application/         # 챌린지 서비스
│   │   ├── domain/              # 챌린지 도메인
│   │   ├── infrastructure/      # 챌린지 데이터 저장소
│   │   └── presentation/        # 챌린지 API
│   ├── experience/              # 경험치 관리
│   │   ├── application/         # 경험치 서비스
│   │   ├── domain/              # 경험치 도메인
│   │   └── infrastructure/      # 경험치 저장소
│   ├── level/                   # 레벨 정의
│   │   ├── application/         # 레벨 서비스
│   │   ├── domain/              # 레벨 도메인
│   │   ├── infrastructure/      # 레벨 저장소
│   │   └── config/              # 초기화 설정
│   └── common/                  # 공통 유틸리티
│       ├── config/              # 공통 설정
│       ├── error/               # 예외 처리
│       └── validation/          # 검증 유틸리티
└── test/                        # 테스트 코드
```

## 토큰 저장소 설정

### 개발 환경 - 로컬 모드 (기본값)
```bash
# 프로파일: dev-local
# 로컬 메모리에 토큰 저장
./gradlew bootRun --args='--spring.profiles.active=dev-local'
```

### 개발 환경 - Redis 모드  
```bash
# 프로파일: dev-redis
# Redis 서버 필요 (없으면 실행 실패)
./gradlew bootRun --args='--spring.profiles.active=dev-redis'
```

### Redis 시작 방법
```bash
# Docker로 Redis 시작
docker run -d --name redis -p 6379:6379 redis:latest

# 또는 로컬 Redis 설치 후 시작
brew install redis
brew services start redis
```

### 운영 환경  
- **staging/prod**: Redis 서버 필수
- 환경변수로 연결 정보 설정:
  - `REDIS_HOST`: Redis 서버 호스트
  - `REDIS_PORT`: Redis 서버 포트  
  - `REDIS_PASSWORD`: Redis 서버 비밀번호

## Docker Compose 실행

### 로컬 개발 환경
```bash
# 1. 애플리케이션 빌드
./gradlew build

# 2. Docker Compose로 전체 서비스 시작
docker-compose -f docker-compose.local.yml up -d

# 3. 헬스 체크 (선택사항)
./health-check.sh
```

### GitHub Container Registry에서 배포
GitHub Actions를 통해 자동으로 빌드된 ARM64 Docker 이미지를 사용하여 배포할 수 있습니다.

```bash
# 1. 환경 변수 파일 설정
cp .env.prod.example .env.prod
# .env.prod 파일에서 GITHUB_REPOSITORY와 POSTGRES_PASSWORD 설정

# 2. GitHub Container Registry에서 이미지 가져와서 실행
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# 3. 로그 확인
docker-compose -f docker-compose.prod.yml logs -f zzic-api
```

### GitHub Actions 워크플로우
- **트리거**: `main`, `develop` 브랜치 푸시 및 태그 생성
- **플랫폼**: ARM64 아키텍처 지원
- **레지스트리**: GitHub Container Registry (ghcr.io)
- **이미지 태그**:
  - `latest`: main 브랜치 최신 커밋
  - `develop`: develop 브랜치 최신 커밋
  - `v*`: 버전 태그 (예: v1.0.0)
  - `<branch>-<sha>`: 브랜치별 커밋 SHA

### 서비스 구성
- **ZZIC API**: http://localhost:8080 (Swagger UI 포함)
- **PostgreSQL**: localhost:5432 (zzic/zzic123)
- **Redis**: localhost:6379

### 유용한 명령어
```bash
# 로그 확인
docker-compose -f docker-compose.local.yml logs -f zzic

# 컨테이너 상태 확인
docker-compose -f docker-compose.local.yml ps

# 서비스 중지
docker-compose -f docker-compose.local.yml down

# 볼륨까지 함께 삭제
docker-compose -f docker-compose.local.yml down -v
```

## 주요 기능

- 사용자 인증 (회원가입, 로그인, 로그아웃, 토큰 갱신)
- 개인 TODO 관리 (생성, 조회, 수정, 삭제)
- 챌린지 관리 (생성, 조회, 수정, 삭제, 참여/탈퇴)
- 챌린지 TODO 관리 (조회, 완료/취소)

## API 명세서

### 인증 (Authentication)
| HTTP Method | Endpoint | 설명 | 요청 Body | 응답 Body | 상태 코드 |
|-------------|----------|------|-----------|-----------|-----------|
| **POST** | `/auth/sign-up` | 회원가입 및 자동 로그인 | `SignUpRequest` | 없음 (쿠키 설정) | `200`: 성공<br>`400`: 잘못된 요청<br>`409`: 이메일 중복 |
| **POST** | `/auth/sign-in` | 로그인 | `SignInRequest` | 없음 (쿠키 설정) | `200`: 성공<br>`401`: 인증 실패 |
| **POST** | `/auth/sign-out` | 로그아웃 | 없음 | 없음 | `200`: 성공 |
| **GET** | `/auth/refresh` | 토큰 갱신 | 없음 | 없음 | `200`: 성공<br>`401`: 갱신 실패 |

### 개인 TODO 관리
| HTTP Method | Endpoint | 설명 | 요청 Body | 응답 Body | 상태 코드 |
|-------------|----------|------|-----------|-----------|-----------|
| **GET** | `/api/members/{memberId}/todos` | TODO 목록 조회 (페이징) | 없음 | `Page<TodoResponse>` | `200`: 성공 |
| **GET** | `/api/members/{memberId}/todos/{id}` | 특정 TODO 조회 | 없음 | `TodoResponse` | `200`: 성공<br>`404`: 찾을 수 없음 |
| **GET** | `/api/members/{memberId}/todos/{id}:{diff}` | 반복 TODO 가상 인스턴스 조회 | 없음 | `TodoResponse` | `200`: 성공<br>`404`: 찾을 수 없음 |
| **POST** | `/api/members/{memberId}/todos` | TODO 생성 | `CreateTodoRequest` | 없음 | `201`: 성공<br>`400`: 잘못된 요청 |
| **PUT** | `/api/members/{memberId}/todos/{id}` | TODO 전체 수정 | `UpdateTodoRequest` | 없음 | `204`: 성공<br>`400`: 잘못된 요청<br>`404`: 찾을 수 없음 |
| **PATCH** | `/api/members/{memberId}/todos/{id}` | TODO 부분 수정 | `UpdateTodoRequest` | 없음 | `204`: 성공<br>`400`: 잘못된 요청<br>`404`: 찾을 수 없음 |
| **DELETE** | `/api/members/{memberId}/todos/{id}` | TODO 삭제 | 없음 | 없음 | `204`: 성공<br>`404`: 찾을 수 없음 |

**쿼리 파라미터:**
- `done`: 완료 상태 필터 (true/false, 기본값: false)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 방식 (기본값: "id,desc")
- `hideStatusIds`: 숨길 상태 ID 목록 (예: `1,2`)

### 챌린지 관리
| HTTP Method | Endpoint | 설명 | 요청 Body | 응답 Body | 상태 코드 |
|-------------|----------|------|-----------|-----------|-----------|
| **POST** | `/challenges` | 챌린지 생성 | `CreateChallengeCommand` | `CreateChallengeResponse` | `200`: 성공 |
| **GET** | `/challenges` | 모든 챌린지 조회 (페이징) | 없음 | `Page<ChallengeDto>` | `200`: 성공 |
| **GET** | `/challenges/{challengeId}` | 특정 챌린지 상세 조회 | 없음 | `ChallengeDto` | `200`: 성공<br>`404`: 찾을 수 없음 |
| **PATCH** | `/challenges/{challengeId}` | 챌린지 수정 | `UpdateChallengeCommand` | 없음 | `200`: 성공<br>`404`: 찾을 수 없음 |
| **DELETE** | `/challenges/{challengeId}` | 챌린지 삭제 | 없음 | 없음 | `200`: 성공<br>`404`: 찾을 수 없음 |
| **GET** | `/challenges/with-participants` | 챌린지 및 참여자 목록 조회 | 없음 | `Page<ChallengeDetailDto>` | `200`: 성공 |

**쿼리 파라미터:**
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 방식 ("latest", "popular", "id,desc", "id,asc")
- `search`: 검색 키워드 (제목, 설명에서 검색)
- `join`: 참여 필터 (true: 참여한 챌린지, false: 참여하지 않은 챌린지)

### 챌린지 참여
| HTTP Method | Endpoint | 설명 | 요청 Body | 응답 Body | 상태 코드 |
|-------------|----------|------|-----------|-----------|-----------|
| **POST** | `/challenge-participations/{challengeId}/join` | 챌린지 참여 | 없음 | 없음 | `200`: 성공<br>`400`: 이미 참여중<br>`404`: 찾을 수 없음 |
| **DELETE** | `/challenge-participations/{challengeId}/leave` | 챌린지 탈퇴 | 없음 | 없음 | `200`: 성공<br>`400`: 참여하지 않음<br>`404`: 찾을 수 없음 |

### 챌린지 TODO
| HTTP Method | Endpoint | 설명 | 요청 Body | 응답 Body | 상태 코드 |
|-------------|----------|------|-----------|-----------|-----------|
| **GET** | `/challenge-todos` | 현재 기간 챌린지 TODO 조회 | 없음 | `Page<ChallengeTodoResponse>` | `200`: 성공 |
| **GET** | `/challenge-todos/uncompleted` | 미완료 챌린지 TODO 조회 | 없음 | `Page<ChallengeTodoResponse>` | `200`: 성공 |
| **GET** | `/challenge-todos/completed` | 완료된 챌린지 TODO 조회 | 없음 | `Page<ChallengeTodoResponse>` | `200`: 성공 |
| **POST** | `/challenge-todos/{challengeId}/complete` | 챌린지 완료 처리 | 없음 | 없음 | `200`: 성공<br>`404`: 찾을 수 없음 |
| **DELETE** | `/challenge-todos/{challengeId}/complete` | 챌린지 완료 취소 | 없음 | 없음 | `200`: 성공<br>`404`: 찾을 수 없음 |

**쿼리 파라미터:**
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 방식 (기본값: "id,desc")

## 인증 방식
- **JWT 토큰**: HTTP-Only 쿠키로 관리
- **리프레시 토큰**: 토큰 갱신을 위한 별도 쿠키
- **보안**: 모든 API는 인증이 필요 (일부 챌린지 조회 API는 선택적 인증)

## 접근 방법
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API 문서**: http://localhost:8080/v3/api-docs

## QA 목표

각 API의 성공 및 실패 시나리오를 테스트하여 시스템의 안정성과 기능성을 검증합니다.

## QA 진행 방법

1. **테스트 준비**
   - Swagger UI (http://localhost:8080/swagger-ui/index.html) 또는 Postman 사용
   - 테스트용 사용자 계정 생성
   - 테스트 데이터 준비

2. **테스트 실행**
   - 각 API 엔드포인트에 대한 정상/비정상 시나리오 테스트
   - 응답 상태 코드 및 데이터 검증
   - 인증이 필요한 API의 경우 토큰 검증

3. **결과 기록**
   - 테스트 결과 문서화
   - 발견된 이슈 추적 및 해결

## 주요 테스트 시나리오

### 인증 테스트
- 회원가입 성공/실패 케이스
- 로그인 성공/실패 케이스
- 토큰 갱신 테스트
- 로그아웃 테스트

### TODO 관리 테스트
- CRUD 작업 테스트
- 페이징 및 정렬 테스트
- 권한 검증 (다른 사용자의 TODO 접근 제한)

### 챌린지 관리 테스트
- 챌린지 생성, 수정, 삭제 테스트
- 챌린지 검색 및 필터링 테스트
- 참여/탈퇴 기능 테스트
- 챌린지 완료 처리 테스트

### 에러 핸들링 테스트
- 잘못된 요청 데이터 처리
- 존재하지 않는 리소스 접근
- 권한 없는 접근 시도
- 서버 오류 상황 처리

## 빠른 시작 가이드

### 1. 프로젝트 클론 및 실행

```bash
# 프로젝트 클론
git clone [repository-url]
cd ZZIC-api

# 애플리케이션 실행 (로컬 모드)
./gradlew bootRun

# 또는 Docker Compose로 실행
./gradlew build
docker-compose -f docker-compose.local.yml up -d
```

### 2. API 테스트

1. **Swagger UI 접속**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
2. **회원가입**: `POST /auth/sign-up`으로 계정 생성
3. **로그인**: 자동으로 로그인되며 쿠키에 토큰 저장
4. **API 테스트**: 인증이 필요한 API들을 테스트

### 3. 개발 시 주의사항

- 개발 환경에서는 H2 인메모리 데이터베이스 사용
- 운영 환경에서는 PostgreSQL과 Redis 필요
- 모든 API는 JWT 토큰 기반 인증 필요 (일부 조회 API 제외)
- 토큰은 HTTP-Only 쿠키로 관리되어 XSS 공격 방지

## DDD 설계 개선 사항

아래는 DDD 관점에서 엔드포인트 및 메서드 설계를 개선하기 위한 주요 제안사항입니다.

- **Presentation과 Application 계층의 DTO 분리**: 현재 `ChallengeController` 등에서 Application Command 객체를 직접 요청 바디로 사용하고 있습니다. Presentation 전용 DTO(Request/Response)를 정의하고 Mapper를 통해 Command/Query 객체로 변환하여 계층 간 결합도를 낮추세요.
- **일관된 엔드포인트 네이밍 및 구조**: `TodoController`는 `/api/members/...` prefix를 사용하지만, `ChallengeController`는 `/challenges`로 prefix 없이 사용합니다. 모든 API에 `/api/v1/` 등의 버전화된 prefix를 적용하고, 리소스 명칭 또한 복수형으로 일관되게 관리하세요.
- **RESTful 리소스 설계 준수**: `/challenge-participations/{challengeId}/join`과 같은 RPC 스타일 엔드포인트 대신 `POST /challenges/{challengeId}/participants` 및 `DELETE /challenges/{challengeId}/participants/{memberId}` 형태로 자원을 명시적으로 표현하세요.
- **공통 Pageable 및 Sort 처리 로직 추출**: 여러 컨트롤러에서 중복된 `Pageable` 생성 로직이 존재합니다. Presentation 계층에서 HandlerMethodArgumentResolver 등을 활용해 자동 바인딩하도록 리팩토링하세요.
- **HTTP Status Code 통일성**: POST 생성 시 `201 Created`, PATCH/PUT/DELETE 무응답 시 `204 No Content`를 일관되게 사용하여 클라이언트 예측 가능성을 높이세요.
- **리소스 계층 네스트 구조 개선**: Challenge Todo API가 최상위 엔드포인트로 분리되어 있지만, DDD 관점에서는 Challenge Aggregate의 서브 리소스로 간주할 수 있습니다. `GET /challenges/{challengeId}/todos` 형태로 계층적 리소스 구조를 검토하세요.
- **필터 및 상태 파라미터 표준화**: 일부 API는 `done`(boolean), 일부는 별도 엔드포인트(`/uncompleted`, `/completed`)를 사용합니다. 쿼리 파라미터(`status=completed|uncompleted`)로 통합하여 API 수를 줄이고 일관성을 확보하세요.
- **API 버전 관리 도입**: Breaking Change를 대비하여 URL 또는 Header 기반 API 버전 관리 전략(v1, v2 등)을 적용하는 것을 권장합니다.