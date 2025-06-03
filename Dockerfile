# 빌드 스테이지 - ARM64 아키텍처 지원
FROM --platform=$BUILDPLATFORM eclipse-temurin:24 AS builder
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

# 런타임 스테이지 - ARM64 아키텍처용
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# 한국 시간대 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 비루트 사용자 생성 (보안 강화)
RUN addgroup -S spring && adduser -S spring -G spring

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 파일 소유권 변경
RUN chown spring:spring app.jar

# 비루트 사용자로 전환
USER spring

# JVM 최적화 옵션
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
