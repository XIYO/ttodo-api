# 로컬 개발 환경 설정 가이드

## 1. Docker Compose로 인프라 실행

Redis와 PostgreSQL만 실행:
```bash
docker-compose up -d
```

서비스 상태 확인:
```bash
docker-compose ps
```

## 2. Spring Boot 애플리케이션 로컬 실행

### 개발 환경으로 실행
```bash
# 환경 변수 파일 로드 후 실행
export $(cat .env.dev | xargs) && ./gradlew bootRun

# 또는 IntelliJ에서 .env.dev 파일을 Environment variables에 설정
```

### 운영 환경으로 실행
```bash
# 환경 변수 파일 로드 후 실행
export $(cat .env.prod | xargs) && ./gradlew bootRun

# SSL 인증서 패스워드 설정 필요
# .env.prod에서 SSL_KEYSTORE_PASSWORD를 실제 값으로 변경하세요
```

## 3. 접속 정보

### 개발 환경
- **애플리케이션**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

### 운영 환경
- **애플리케이션**: https://localhost:8443
- **PostgreSQL**: localhost:5432 (동일)
- **Redis**: localhost:6379 (동일)

## 4. 인프라 종료

```bash
# 컨테이너 중지
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v
```

## 5. 주의사항

1. **SSL 인증서**: .env.prod의 `SSL_KEYSTORE_PASSWORD`를 실제 값으로 설정
2. **방화벽**: macOS에서 443 포트 사용 시 관리자 권한 필요할 수 있음
3. **데이터 영속성**: PostgreSQL 데이터는 Docker 볼륨에 저장됨
