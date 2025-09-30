# TTODO-BACK

TTODO의 백엔드 API 서버입니다. 개인 TODO 관리와 챌린지를 통한 동기부여 기능을 제공합니다.

## 📚 아키텍처 문서

- **[TTODO 아키텍처 가이드](TTODO_ARCHITECTURE_GUIDE.md)** - TTODO 아키텍처 패턴과 핵심 원칙
- **[개발 가이드라인](DEVELOPMENT_GUIDELINES.md)** - 코딩 표준, 테스트 전략, Git 워크플로우
- **[아키텍처 검증 체크리스트](ARCHITECTURE_CHECKLIST.md)** - 아키텍처 준수 여부 검증
- **[개발 로드맵](ROADMAP.md)** - 향후 개발 계획 및 우선순위
- **[Claude Code 가이드](CLAUDE.md)** - Claude Code AI 개발 가이드

## 기술 스택

### Backend

- **Java 21** - LTS 버전 Java 사용
- **Spring Boot 3.5.0** - 백엔드 프레임워크
- **Spring Security** - 인증/인가 처리
- **Spring Data JPA** - ORM 및 데이터베이스 연동
- **Spring Data Redis** - 토큰 저장소 및 캐싱
- **Spring Validation** - 입력 데이터 검증

### Database

- **PostgreSQL** - 메인 데이터베이스 (개발/운영)
- **Redis** - 토큰 저장소 및 세션 관리

### Documentation & Testing

- **Swagger/OpenAPI 3** - API 문서화
- **JUnit 5** - 단위 테스트
- **Spring Boot Test** - 슬라이스 테스트 (@WebMvcTest, Mockito 기반)
- 통합 테스트 및 Testcontainers 제거하여 테스트 단순화 및 실행 속도 개선

### Libraries

- **Lombok** - 보일러플레이트 코드 제거
- **MapStruct** - 객체 매핑
- **JWT** - 토큰 기반 인증
- **OWASP Java HTML Sanitizer** - XSS 방지
- **JPA Criteria API** - 타입 안전 동적 쿼리

### DevOps

- **Docker** - 컨테이너화
- **Docker Compose** - 로컬 개발 환경
- **GitHub Actions** - CI/CD
- **GitHub Container Registry** - 이미지 저장소

### Logging

- **Log4j2** - 콘솔과 파일로 비동기 로깅
- `LoggingAspect`가 서비스와 컨트롤러 메서드의 진입과 종료를 기록합니다.
- 상세 설정은 `log4j2-spring.xml`과 `log4j2-test.xml`에 정의되어 있습니다.

## 프로젝트 구조

```
src/
├── main/java/point/ttodoApi/
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

## 개발 환경 설정

### 필수 요구사항

- JDK 21
- Docker Desktop

### 빠른 시작

```bash
# 프로젝트 클론
git clone [repository-url]
cd TTODO-api

# 애플리케이션 실행
./gradlew bootRun
```

**자동으로 처리되는 것들:**

- PostgreSQL Docker 컨테이너 자동 시작/종료 (Spring Boot Docker Compose 통합)
- 데이터베이스 스키마 생성
- 초기 데이터 설정
- Redis 컨테이너 시작 (docker-compose.yml에 정의된 경우)

### IDE에서 실행

- **IntelliJ IDEA**: 프로젝트 열고 `TtodoApiApplication` 실행
- **VS Code**: Terminal에서 `./gradlew bootRun`
- **Eclipse**: Run As > Spring Boot App, Profile: `dev` 설정

### 데이터베이스 접속 정보

```yaml
# PostgreSQL (자동 실행됨)
Host: localhost
Port: 5432
Database: ttodo_dev
Username: ttodo_user
Password: ttodo_password
```

### 테스트 실행

```bash
./gradlew test
```

- Testcontainers를 통해 PostgreSQL이 자동으로 실행됩니다
- 테스트 종료 후 컨테이너는 자동으로 정리됩니다

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
docker-compose -f docker-compose.prod.yml logs -f ttodo-api
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

- **TTODO API**: http://localhost:8080 (Swagger UI 포함)
- **PostgreSQL**: localhost:5432 (ttodo/ttodo123)
- **Redis**: localhost:6379

### 유용한 명령어

```bash
# 로그 확인
docker-compose -f docker-compose.local.yml logs -f ttodo

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
- 동적 쿼리 시스템 (타입 안전 검색, SQL Injection 방지)
- 입력값 검증 시스템 (커스텀 어노테이션, XSS 방지)
- 에러 처리 시스템 (RFC 7807 표준)

## API 명세서

### 개발 환경 테스트 토큰

### Quick API Examples (form-only)

- Create todo (application/x-www-form-urlencoded)

```
curl -X POST http://localhost:8080/todos \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "title=주 2회 운동" \
  --data-urlencode "priorityId=1" \
  --data-urlencode "date=2025-01-01" \
  --data-urlencode 'recurrenceRuleJson={"frequency":"WEEKLY","interval":1,"byWeekDays":["MO","TH"],"endCondition":{"type":"UNTIL","until":"2025-12-31"},"anchorDate":"2025-01-01","timezone":"Asia/Seoul"}'
```

- Update todo (PUT, form)

```
curl -X PUT http://localhost:8080/todos/82:0 \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "title=주 3회 운동" \
  --data-urlencode 'recurrenceRuleJson={"frequency":"WEEKLY","interval":1,"byWeekDays":["MO","WE","FR"],"endCondition":{"type":"COUNT","count":24}}'
```

- Patch complete only (form)

```
curl -X PATCH http://localhost:8080/todos/82:3 \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "complete=true"
```

Notes

- Inputs are accepted only as `application/x-www-form-urlencoded` or `multipart/form-data`.
- `recurrenceRuleJson` is a JSON string field inside the form.

개발 환경에서 API 테스트를 위한 하드코딩된 JWT 토큰입니다. **이 토큰은 사실상 만료가 없습니다 (100년 후 만료).**

**토큰 정보:**

- User ID: `ffffffff-ffff-ffff-ffff-ffffffffffff` (익명 사용자)
- Email: `anon@ttodo.dev`
- Nickname: `익명사용자`
- TimeZone: `Asia/Seoul`
- Locale: `ko_KR`
- Device ID: `test-device-anon`
- **만료: 개발 환경에서만 사용**

**개발용 Access Token 사용법:**

```bash
# 환경변수로 개발 토큰 설정 (보안을 위해 토큰은 별도 문서 참조)
export DEV_TOKEN="[개발 환경 토큰 - SECURITY_NOTES.md 참조]"
```

**curl 명령어 예시:**

```bash
# API 테스트용 curl 명령어 (쿠키 방식)
curl -H "Cookie: access-token=$DEV_TOKEN" \
     http://localhost:8080/todos

# Bearer 토큰 방식
curl -H "Authorization: Bearer $DEV_TOKEN" \
     http://localhost:8080/todos
```

**Swagger UI에서 사용하기:**

1. Swagger UI 접속: http://localhost:8080/swagger-ui.html
2. 상단의 "Authorize" 버튼 클릭
3. 개발 토큰 값 입력 (Bearer 접두사 제외, SECURITY_NOTES.md 참조)
4. Authorize 클릭

**주의사항:**

- 이 토큰은 개발/테스트 환경에서만 사용하세요
- 프로덕션 환경에서는 절대 사용하지 마세요
- 이 토큰은 `test-private.pem` 키로 서명되었습니다

### 인증 (Authentication)

| HTTP Method | Endpoint         | 설명            | 요청 Body         | 응답 Body    | 상태 코드                                       |
|-------------|------------------|---------------|-----------------|------------|---------------------------------------------|
| **POST**    | `/auth/sign-up`  | 회원가입 및 자동 로그인 | `SignUpRequest` | 없음 (쿠키 설정) | `200`: 성공<br>`400`: 잘못된 요청<br>`409`: 이메일 중복 |
| **POST**    | `/auth/sign-in`  | 로그인           | `SignInRequest` | 없음 (쿠키 설정) | `200`: 성공<br>`401`: 인증 실패                   |
| **POST**    | `/auth/sign-out` | 로그아웃          | 없음              | 없음         | `200`: 성공                                   |
| **GET**     | `/auth/refresh`  | 토큰 갱신         | 없음              | 없음         | `200`: 성공<br>`401`: 갱신 실패                   |

### 개인 TODO 관리

| HTTP Method | Endpoint                                    | 설명                 | 요청 Body             | 응답 Body              | 상태 코드                                        |
|-------------|---------------------------------------------|--------------------|---------------------|----------------------|----------------------------------------------|
| **GET**     | `/api/members/{memberId}/todos`             | TODO 목록 조회 (페이징)   | 없음                  | `Page<TodoResponse>` | `200`: 성공                                    |
| **GET**     | `/api/members/{memberId}/todos/{id}`        | 특정 TODO 조회         | 없음                  | `TodoResponse`       | `200`: 성공<br>`404`: 찾을 수 없음                  |
| **GET**     | `/api/members/{memberId}/todos/{id}:{diff}` | 반복 TODO 가상 인스턴스 조회 | 없음                  | `TodoResponse`       | `200`: 성공<br>`404`: 찾을 수 없음                  |
| **POST**    | `/api/members/{memberId}/todos`             | TODO 생성            | `CreateTodoRequest` | 없음                   | `201`: 성공<br>`400`: 잘못된 요청                   |
| **PUT**     | `/api/members/{memberId}/todos/{id}`        | TODO 전체 수정         | `UpdateTodoRequest` | 없음                   | `204`: 성공<br>`400`: 잘못된 요청<br>`404`: 찾을 수 없음 |
| **PATCH**   | `/api/members/{memberId}/todos/{id}`        | TODO 부분 수정         | `UpdateTodoRequest` | 없음                   | `204`: 성공<br>`400`: 잘못된 요청<br>`404`: 찾을 수 없음 |
| **DELETE**  | `/api/members/{memberId}/todos/{id}`        | TODO 삭제            | 없음                  | 없음                   | `204`: 성공<br>`404`: 찾을 수 없음                  |

**쿼리 파라미터:**

- `done`: 완료 상태 필터 (true/false, 기본값: false)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 방식 (기본값: "id,desc")
- `hideStatusIds`: 숨길 상태 ID 목록 (예: `1,2`)

### 챌린지 관리

| HTTP Method | Endpoint                        | 설명              | 요청 Body                  | 응답 Body                    | 상태 코드                       |
|-------------|---------------------------------|-----------------|--------------------------|----------------------------|-----------------------------|
| **POST**    | `/challenges`                   | 챌린지 생성          | `CreateChallengeCommand` | `CreateChallengeResponse`  | `200`: 성공                   |
| **GET**     | `/challenges`                   | 모든 챌린지 조회 (페이징) | 없음                       | `Page<ChallengeDto>`       | `200`: 성공                   |
| **GET**     | `/challenges/{challengeId}`     | 특정 챌린지 상세 조회    | 없음                       | `ChallengeDto`             | `200`: 성공<br>`404`: 찾을 수 없음 |
| **PATCH**   | `/challenges/{challengeId}`     | 챌린지 수정          | `UpdateChallengeCommand` | 없음                         | `200`: 성공<br>`404`: 찾을 수 없음 |
| **DELETE**  | `/challenges/{challengeId}`     | 챌린지 삭제          | 없음                       | 없음                         | `200`: 성공<br>`404`: 찾을 수 없음 |
| **GET**     | `/challenges/with-participants` | 챌린지 및 참여자 목록 조회 | 없음                       | `Page<ChallengeDetailDto>` | `200`: 성공                   |

**쿼리 파라미터:**

- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 방식 ("latest", "popular", "id,desc", "id,asc")
- `search`: 검색 키워드 (제목, 설명에서 검색)
- `join`: 참여 필터 (true: 참여한 챌린지, false: 참여하지 않은 챌린지)

### 챌린지 참여

| HTTP Method | Endpoint                                        | 설명     | 요청 Body | 응답 Body | 상태 코드                                         |
|-------------|-------------------------------------------------|--------|---------|---------|-----------------------------------------------|
| **POST**    | `/challenge-participations/{challengeId}/join`  | 챌린지 참여 | 없음      | 없음      | `200`: 성공<br>`400`: 이미 참여중<br>`404`: 찾을 수 없음  |
| **DELETE**  | `/challenge-participations/{challengeId}/leave` | 챌린지 탈퇴 | 없음      | 없음      | `200`: 성공<br>`400`: 참여하지 않음<br>`404`: 찾을 수 없음 |

### 챌린지 TODO

| HTTP Method | Endpoint                                  | 설명                | 요청 Body | 응답 Body                       | 상태 코드                       |
|-------------|-------------------------------------------|-------------------|---------|-------------------------------|-----------------------------|
| **GET**     | `/challenge-todos`                        | 현재 기간 챌린지 TODO 조회 | 없음      | `Page<ChallengeTodoResponse>` | `200`: 성공                   |
| **GET**     | `/challenge-todos/uncompleted`            | 미완료 챌린지 TODO 조회   | 없음      | `Page<ChallengeTodoResponse>` | `200`: 성공                   |
| **GET**     | `/challenge-todos/completed`              | 완료된 챌린지 TODO 조회   | 없음      | `Page<ChallengeTodoResponse>` | `200`: 성공                   |
| **POST**    | `/challenge-todos/{challengeId}/complete` | 챌린지 완료 처리         | 없음      | 없음                            | `200`: 성공<br>`404`: 찾을 수 없음 |
| **DELETE**  | `/challenge-todos/{challengeId}/complete` | 챌린지 완료 취소         | 없음      | 없음                            | `200`: 성공<br>`404`: 찾을 수 없음 |

**쿼리 파라미터:**

- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)
- `sort`: 정렬 방식 (기본값: "id,desc")

### 통합 검색 API (동적 쿼리 시스템)

| HTTP Method | Endpoint                            | 설명           | 쿼리 파라미터                                                         | 응답 Body           | 상태 코드     |
|-------------|-------------------------------------|--------------|-----------------------------------------------------------------|-------------------|-----------|
| **GET**     | `/search/todos`                     | Todo 검색      | keyword, complete, categoryIds, priorityIds, startDate, endDate | `Page<Todo>`      | `200`: 성공 |
| **GET**     | `/search/members`                   | 멤버 검색 (관리자)  | emailKeyword, nicknameKeyword, role, lastLoginFrom, lastLoginTo | `Page<Member>`    | `200`: 성공 |
| **GET**     | `/search/categories`                | 카테고리 검색      | titleKeyword, colorCode, shareTypes, includeSubCategories       | `Page<Category>`  | `200`: 성공 |
| **GET**     | `/search/challenges`                | 챌린지 검색       | titleKeyword, visibility, periodType, ongoingOnly, joinableOnly | `Page<Challenge>` | `200`: 성공 |
| **GET**     | `/search/todos/today-incomplete`    | 오늘의 미완료 Todo | 없음                                                              | `List<Todo>`      | `200`: 성공 |
| **GET**     | `/search/members/inactive`          | 비활성 회원 조회    | days (기본값: 90)                                                  | `Page<Member>`    | `200`: 성공 |
| **GET**     | `/search/challenges/public-ongoing` | 공개 진행중 챌린지   | 없음                                                              | `Page<Challenge>` | `200`: 성공 |

## 인증 방식

- **JWT 토큰**: HTTP-Only 쿠키로 관리
- **리프레시 토큰**: 토큰 갱신을 위한 별도 쿠키
- **보안**: 모든 API는 인증이 필요 (일부 챌린지 조회 API는 선택적 인증)

## 접근 방법

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API 문서**: http://localhost:8080/v3/api-docs

## QA 목표

현재 빌드는 최소주의(minimal) 전략을 채택하여 SonarQube, Jacoco, OpenRewrite 등 추가 품질/커버리지 도구를 제거했습니다. 품질은 코드 리뷰, 통합 테스트, Testcontainers 격리 실행, 그리고 도메인 규칙 검증 테스트로 관리합니다.

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
cd TTODO-api

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

- 모든 환경에서 PostgreSQL과 Redis 사용 (Spring Boot Docker Compose 자동 실행)
- 개발 환경에서는 Docker Compose로 자동 관리
- 모든 API는 JWT 토큰 기반 인증 필요 (일부 조회 API 제외)
- 토큰은 HTTP-Only 쿠키로 관리되어 XSS 공격 방지

## DDD 설계 개선 사항

아래는 DDD 관점에서 엔드포인트 및 메서드 설계를 개선하기 위한 주요 제안사항입니다.

- **Presentation과 Application 계층의 DTO 분리**: 현재 `ChallengeController` 등에서 Application Command 객체를 직접 요청 바디로 사용하고 있습니다.
  Presentation 전용 DTO(Request/Response)를 정의하고 Mapper를 통해 Command/Query 객체로 변환하여 계층 간 결합도를 낮추세요.
- **일관된 엔드포인트 네이밍 및 구조**: `TodoController`는 `/api/members/...` prefix를 사용하지만, `ChallengeController`는 `/challenges`로
  prefix 없이 사용합니다. 모든 API에 `/api/v1/` 등의 버전화된 prefix를 적용하고, 리소스 명칭 또한 복수형으로 일관되게 관리하세요.
- **RESTful 리소스 설계 준수**: `/challenge-participations/{challengeId}/join`과 같은 RPC 스타일 엔드포인트 대신
  `POST /challenges/{challengeId}/participants` 및 `DELETE /challenges/{challengeId}/participants/{memberId}` 형태로 자원을
  명시적으로 표현하세요.
- **공통 Pageable 및 Sort 처리 로직 추출**: 여러 컨트롤러에서 중복된 `Pageable` 생성 로직이 존재합니다. Presentation 계층에서
  HandlerMethodArgumentResolver 등을 활용해 자동 바인딩하도록 리팩토링하세요.
- **HTTP Status Code 통일성**: POST 생성 시 `201 Created`, PATCH/PUT/DELETE 무응답 시 `204 No Content`를 일관되게 사용하여 클라이언트 예측 가능성을
  높이세요.
- **리소스 계층 네스트 구조 개선**: Challenge Todo API가 최상위 엔드포인트로 분리되어 있지만, DDD 관점에서는 Challenge Aggregate의 서브 리소스로 간주할 수 있습니다.
  `GET /challenges/{challengeId}/todos` 형태로 계층적 리소스 구조를 검토하세요.
- **필터 및 상태 파라미터 표준화**: 일부 API는 `done`(boolean), 일부는 별도 엔드포인트(`/uncompleted`, `/completed`)를 사용합니다. 쿼리 파라미터(
  `status=completed|uncompleted`)로 통합하여 API 수를 줄이고 일관성을 확보하세요.
- **API 버전 관리 도입**: Breaking Change를 대비하여 URL 또는 Header 기반 API 버전 관리 전략(v1, v2 등)을 적용하는 것을 권장합니다.
