# ZZIC-BACK

ZZIC 의 백엔드 서버입니다.

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

## 구현 목표

- TODO 생성
- TODO 조회
- TODO 수정
- TODO 삭제

## API 명세서

| HTTP Method | Endpoint         | 설명                     | 요청 Body            | 응답 Body             | 상태 코드 및 설명                        |
|-------------|------------------|--------------------------|----------------------|-----------------------|-----------------------------------------|
| **GET**     | `/api/todo`      | Todo 목록 조회           | 없음                 | `List<TodoMainResponse>` | `200`: 성공적으로 Todo 목록을 조회함    |
| **GET**     | `/api/todo/{id}` | 특정 Todo 조회           | 없음                 | `Todo`                | `200`: 성공적으로 Todo를 조회함<br>`404`: 해당 ID의 Todo를 찾을 수 없음 |
| **POST**    | `/api/todo`      | Todo 등록                | `CreateTodoRequest`  | 없음                  | `201`: 성공적으로 Todo를 생성함<br>`400`: 잘못된 요청 데이터 |
| **PUT**     | `/api/todo/{id}` | Todo 수정                | `UpdateTodoRequest`  | 없음                  | `204`: 성공적으로 Todo를 수정함<br>`400`: 잘못된 요청 데이터<br>`404`: 해당 ID의 Todo를 찾을 수 없음 |
| **DELETE**  | `/api/todo/{id}` | Todo 삭제                | 없음                 | 없음                  | `204`: 성공적으로 Todo를 삭제함<br>`404`: 해당 ID의 Todo를 찾을 수 없음 |

## QA 목표

- 각 API의 성공 및 실패 시나리오를 테스트합니다.
- 예상 응답 코드와 실제 응답 코드가 일치하는지 확인합니다.
- 요청 및 응답 데이터의 유효성을 검증합니다.

## QA Checklist

| 체크 | HTTP Method | Endpoint         | 테스트 케이스 설명                            | 예상 상태 코드 | 테스트 결과 |
|------|-------------|------------------|-----------------------------------------------|----------------|-------------|
| [ ]  | **GET**     | `/api/todo`      | Todo 목록을 성공적으로 조회합니다.             | `200 OK`       |             |
| [ ]  | **GET**     | `/api/todo/{id}` | 특정 Todo를 성공적으로 조회합니다.             | `200 OK`       |             |
| [ ]  | **GET**     | `/api/todo/{id}` | 존재하지 않는 Todo ID로 조회 시 오류 발생.     | `404 Not Found`|             |
| [ ]  | **POST**    | `/api/todo`      | 올바른 데이터를 사용하여 Todo를 성공적으로 생성합니다. | `201 Created`  |             |
| [ ]  | **POST**    | `/api/todo`      | 잘못된 데이터를 사용하여 Todo 생성 시 오류 발생. | `400 Bad Request`|             |
| [ ]  | **PUT**     | `/api/todo/{id}` | 특정 Todo를 성공적으로 수정합니다.             | `204 No Content`|             |
| [ ]  | **PUT**     | `/api/todo/{id}` | 존재하지 않는 Todo ID로 수정 시 오류 발생.     | `404 Not Found`|             |
| [ ]  | **PUT**     | `/api/todo/{id}` | 잘못된 데이터를 사용하여 Todo 수정 시 오류 발생.| `400 Bad Request`|             |
| [ ]  | **DELETE**  | `/api/todo/{id}` | 특정 Todo를 성공적으로 삭제합니다.             | `204 No Content`|             |
| [ ]  | **DELETE**  | `/api/todo/{id}` | 존재하지 않는 Todo ID로 삭제 시 오류 발생.     | `404 Not Found`|             |

## QA 진행 방법

1. **테스트 준비:**
    - Swagger 또는 Postman을 사용하여 각 API에 대한 요청을 준비합니다.
    - 테스트 데이터를 사전에 설정합니다.

2. **테스트 실행:**
    - 각 테스트 케이스에 대해 API 요청을 실행합니다.
    - 응답 상태 코드와 데이터를 확인합니다.

3. **결과 기록:**
    - 각 테스트 케이스의 결과를 테이블의 "테스트 결과" 열에 기록합니다.
    - 성공한 테스트는 체크박스를 활성화합니다.

4. **이슈 발생 시:**
    - 발견된 문제를 기록하고, 개발자와 공유하여 수정합니다.