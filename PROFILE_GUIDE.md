# Spring Boot 프로파일 설정 가이드

## 프로파일 구조

이 프로젝트는 3개의 환경별 프로파일 그룹으로 구성되어 있습니다:

### 1. dev (개발 환경)
- **데이터베이스**: H2 (인메모리/파일 기반)
- **Redis**: Embedded Redis (포트: 6370)
- **로깅**: DEBUG 레벨
- **SSL**: 비활성화

활성화되는 프로파일:
- `db-h2`: H2 데이터베이스 설정
- `redis-embedded`: 임베디드 Redis 설정
- `common-dev`: 개발 환경 공통 설정

### 2. staging (스테이징 환경)
- **데이터베이스**: PostgreSQL
- **Redis**: 외부 Redis 서버
- **로깅**: INFO 레벨
- **SSL**: 설정 가능

활성화되는 프로파일:
- `db-pg`: PostgreSQL 데이터베이스 설정
- `redis-server`: 외부 Redis 서버 설정
- `common-staging`: 스테이징 환경 공통 설정

### 3. prod (운영 환경)
- **데이터베이스**: PostgreSQL
- **Redis**: 외부 Redis 서버
- **로깅**: WARN 레벨
- **SSL**: 활성화
- **포트**: 443 (HTTPS)

활성화되는 프로파일:
- `db-pg`: PostgreSQL 데이터베이스 설정
- `redis-server`: 외부 Redis 서버 설정
- `common-prod`: 운영 환경 공통 설정

## 프로파일 실행 방법

### 1. IDE에서 실행
`application.yml`에서 기본 프로파일이 `dev`로 설정되어 있어 별도 설정 없이 개발 환경으로 실행됩니다.

### 2. JAR 실행 시
```bash
# 개발 환경
java -jar -Dspring.profiles.active=dev zzic-api.jar

# 스테이징 환경
java -jar -Dspring.profiles.active=staging zzic-api.jar

# 운영 환경
java -jar -Dspring.profiles.active=prod zzic-api.jar
```

### 3. Gradle 실행 시
```bash
# 개발 환경 (기본)
./gradlew bootRun

# 스테이징 환경
./gradlew bootRun --args='--spring.profiles.active=staging'

# 운영 환경
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 환경 변수 설정

### 스테이징/운영 환경에서 필요한 환경 변수:

#### 데이터베이스
- `DATABASE_URL`: PostgreSQL 연결 URL
- `DATABASE_USERNAME`: 데이터베이스 사용자명
- `DATABASE_PASSWORD`: 데이터베이스 비밀번호

#### Redis
- `REDIS_HOST`: Redis 서버 호스트
- `REDIS_PORT`: Redis 서버 포트
- `REDIS_PASSWORD`: Redis 비밀번호

#### JWT
- `JWT_PRIVATE_KEY`: JWT 개인키 경로
- `JWT_PUBLIC_KEY`: JWT 공개키 경로
- `JWT_KEY_ID`: JWT 키 ID
- `JWT_EXPIRATION`: 액세스 토큰 만료 시간(분)
- `JWT_REFRESH_EXPIRATION`: 리프레시 토큰 만료 시간(초)
- `JWT_COOKIE_DOMAIN`: 쿠키 도메인
- `JWT_COOKIE_SECURE`: 쿠키 보안 설정
- `JWT_COOKIE_HTTP_ONLY`: HttpOnly 설정
- `JWT_COOKIE_SAME_SITE`: SameSite 설정
- `JWT_COOKIE_MAX_AGE`: 쿠키 최대 수명

## 설정 파일 구조

```
resources/
├── application.yml                 # 공통 설정 및 프로파일 그룹 정의
├── application-db-h2.yml          # H2 데이터베이스 설정
├── application-db-pg.yml          # PostgreSQL 설정
├── application-redis-embedded.yml  # 임베디드 Redis 설정
├── application-redis-server.yml   # 외부 Redis 서버 설정
├── application-common-dev.yml     # 개발 환경 공통 설정
├── application-common-staging.yml # 스테이징 환경 공통 설정
└── application-common-prod.yml    # 운영 환경 공통 설정
```

## 주요 특징

1. **프로파일 그룹화**: Spring Boot 2.4+의 profile groups 기능 사용
2. **환경별 분리**: 개발/스테이징/운영 환경별 완전 분리
3. **Embedded Redis**: 개발 환경에서는 별도 Redis 설치 불필요
4. **환경 변수 지원**: 운영 환경에서는 환경 변수로 설정 오버라이드
5. **보안 설정**: 환경별 SSL 및 쿠키 보안 설정 차별화
