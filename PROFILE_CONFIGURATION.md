# ZZIC API 프로필 구성 가이드

## 개요
이 문서는 ZZIC API의 새로운 프로필 구성에 대해 설명합니다.

## 프로필 구조

### 1. 메인 프로필
- **dev**: 개발 환경 (H2 + LocalTokenRepository)
- **prod**: 운영 환경 (PostgreSQL + RedisTokenRepository)

### 2. 프로필 그룹 구성

#### dev 프로필
```
dev:
  - db-h2
  - storage-local  
  - common-dev
```

#### prod 프로필
```
prod:
  - db-pg
  - storage-redis
  - common-prod
```

## 설정 파일 구조

### 공통 설정
- `application.yml`: 기본 설정 및 프로필 그룹 정의

### 데이터베이스 설정
- `application-db-h2.yml`: H2 데이터베이스 설정
- `application-db-pg.yml`: PostgreSQL 데이터베이스 설정

### 토큰 저장소 설정
- `application-storage-local.yml`: 인메모리 토큰 저장소 설정
- `application-storage-redis.yml`: Redis 토큰 저장소 설정

### 환경별 공통 설정
- `application-common-dev.yml`: 개발 환경 공통 설정 (JWT, 로깅 등)
- `application-common-prod.yml`: 운영 환경 공통 설정 (JWT, 로깅 등)

## JWT 쿠키 설정

### 개발 환경 (dev)
```yaml
jwt:
  access-token:
    cookie:
      name: access-token
      domain: localhost
      path: /
      secure: false
      httpOnly: false
      sameSite: none
  refresh-token:
    cookie:
      name: refresh-token
      domain: localhost
      path: /
      secure: false
      httpOnly: true
      sameSite: none
```

### 운영 환경 (prod)
```yaml
jwt:
  access-token:
    cookie:
      name: access-token
      domain: ${JWT_COOKIE_DOMAIN:xiyo.dev}
      path: /
      secure: true
      httpOnly: false
      sameSite: lax
  refresh-token:
    cookie:
      name: refresh-token
      domain: ${JWT_COOKIE_DOMAIN:xiyo.dev}
      path: /
      secure: true
      httpOnly: true
      sameSite: lax
```

## 토큰 저장소 Repository 패턴

### TokenRepository 인터페이스
```java
public interface TokenRepository {
    void save(String key, String value, long timeoutSeconds);
    String get(String key);
    void delete(String key);
}
```

### 구현체
1. **LocalTokenRepository**: 인메모리 ConcurrentHashMap 기반
   - 조건: `storage.type=local`
   - 개발 환경에서 사용

2. **RedisTokenRepository**: Redis 기반
   - 조건: `storage.type=redis`
   - 운영 환경에서 사용

## 사용법

### 개발 환경 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 운영 환경 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 주요 변경사항

1. **프로필 단순화**: staging 제거, dev/prod로 이원화
2. **JWT 쿠키 분리**: access-token과 refresh-token 별도 설정
3. **Repository 패턴**: TokenRepository 인터페이스 기반 의존성 주입
4. **조건부 Bean**: @ConditionalOnProperty를 통한 환경별 Bean 로딩
5. **Embedded Redis 제거**: 실제 Redis만 사용

## 보안 설정

### 개발 환경
- secure: false (HTTP 허용)
- sameSite: none (크로스 오리진 허용)
- access-token httpOnly: false (JS 접근 허용)

### 운영 환경  
- secure: true (HTTPS 필수)
- sameSite: lax (CSRF 보호)
- access-token httpOnly: false (JS 접근 허용)
- refresh-token httpOnly: true (XSS 보호)
