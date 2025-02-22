# Java 21 기반 실행환경
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# 빌드 시 전달할 변수 (빌드 중에는 ARG 사용)
ARG JAR_FILE

# JAR 파일 복사
COPY build/libs/${JAR_FILE} app.jar

# 환경 변수 기본값 설정
ENV DATABASE_URL="jdbc:h2:mem:testdb"
ENV DATABASE_USERNAME="sa"
ENV DATABASE_PASSWORD=""
ENV SPRING_PROFILES_ACTIVE="local"

# 실행 시 환경 변수가 없으면 기본값 사용
ENTRYPOINT ["sh", "-c", "java -jar \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    -DDATABASE_URL=${DATABASE_URL} \
    -DDATABASE_USERNAME=${DATABASE_USERNAME} \
    -DDATABASE_PASSWORD=${DATABASE_PASSWORD} \
    app.jar"]