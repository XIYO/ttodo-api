# 런타임 스테이지 - ARM64 아키텍처용
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app

# GitHub Actions에서 빌드된 JAR 파일 복사
COPY build/libs/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
