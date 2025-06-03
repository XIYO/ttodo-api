
# 빌드 스테이지
FROM eclipse-temurin:24 AS builder
WORKDIR /app

# Gradle wrapper 및 설정 파일들 먼저 복사 (종속성 캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Gradle wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 종속성 다운로드 (소스 코드 변경 시에도 캐시 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 프로젝트 빌드 (테스트 제외)
RUN ./gradlew bootJar -x test --no-daemon

# 런타임 스테이지
FROM eclipse-temurin:24-jre
WORKDIR /app

# 비루트 사용자 생성 (보안 강화)
RUN groupadd -r spring && useradd -r -g spring spring

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 파일 소유권 변경
RUN chown spring:spring app.jar

# 비루트 사용자로 전환
USER spring

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]