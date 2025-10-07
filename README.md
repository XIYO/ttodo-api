# TTODO-API - Backend

**로컬 퍼스트 Todo 애플리케이션의 백엔드 API 서버**

DDD(Domain-Driven Design) 아키텍처를 따르는 Spring Boot 기반 REST API입니다.

---

## 🚀 빠른 시작

```bash
# Docker Compose로 인프라 시작 (PostgreSQL + Redis)
docker-compose -f docker-compose.local.yml up -d

# Spring Boot 실행
./gradlew bootRun

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## 🏗️ 기술 스택

| 레이어 | 기술 | 용도 |
|--------|------|------|
| **Language** | Java 21 | LTS 버전 |
| **Framework** | Spring Boot 3.5 | REST API |
| **Database** | PostgreSQL 18 | 메인 DB |
| **Cache** | Redis | 세션, 멱등성 키 |
| **Auth** | JWT (RSA) | 인증/인가 |
| **ORM** | Spring Data JPA | 데이터 접근 |
| **Docs** | Swagger/OpenAPI 3 | API 문서 |
| **Testing** | JUnit 5 + Mockito | 단위/슬라이스 테스트 |

---

## 📂 프로젝트 구조 (DDD)

```
src/main/java/point/ttodoApi/
├── auth/                 # 인증/인가
│   ├── application/     # 서비스
│   ├── domain/          # 도메인
│   ├── infrastructure/  # 리포지토리
│   └── presentation/    # 컨트롤러
├── todo/                # TODO 관리
├── challenge/           # 챌린지
├── experience/          # 경험치/레벨
└── common/              # 공통 유틸리티
```

---

## 🛠️ 개발 스크립트

```bash
./gradlew bootRun        # 애플리케이션 실행
./gradlew test           # 테스트 실행
./gradlew build          # 빌드
./gradlew clean build    # 클린 빌드

# Docker
docker-compose -f docker-compose.local.yml up -d      # 인프라 시작
docker-compose -f docker-compose.local.yml logs -f    # 로그 확인
docker-compose -f docker-compose.local.yml down       # 중지
```

---

## 📡 주요 API

### 인증
- `POST /auth/sign-up` - 회원가입
- `POST /auth/sign-in` - 로그인
- `POST /auth/refresh` - 토큰 갱신
- `POST /auth/sign-out` - 로그아웃

### TODO
- `GET /todos` - 목록 조회
- `POST /todos` - 생성
- `PUT /todos/{id}` - 수정
- `DELETE /todos/{id}` - 삭제

**상세 API**: http://localhost:8080/swagger-ui.html

---

## 🔗 문서

**프로젝트 전체 문서는 루트에서 관리됩니다:**

- **[프로젝트 README](../README.md)** - 전체 개요
- **[아키텍처](../ARCHITECTURE.md)** - 시스템 설계
- **[로드맵](../ROADMAP.md)** - 개발 계획
- **[다음 작업](../docs/01-NEXT-STEPS.md)** - 현재 작업 ⭐
- **[개발 환경 설정](../docs/00-DEVELOPMENT-SETUP.md)** - 환경 구성
- **[ADR](../docs/adr/)** - 아키텍처 결정 기록
- **[컨트리뷰션](../CONTRIBUTING.md)** - 개발 규칙

**Backend 전용 문서:**

- **[Claude Code 가이드](CLAUDE.md)** - AI 개발 도구 사용법 ⭐
- **[테스트 표준](docs/testing-standards.md)** - 테스트 작성 가이드
- **[보안 노트](SECURITY_NOTES.md)** - JWT 키, 개발 토큰

---

## 🔐 인증 방식

- **JWT 토큰**: HTTP-Only 쿠키
- **RSA 서명**: 변조 방지
- **세션 추적**: Redis 기반

---

## 📄 라이선스

MIT License
