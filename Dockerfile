# Java 21 기반 실행환경
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 실행 시 환경 변수를 인자로 받을 수 있도록 설정
ENTRYPOINT ["sh", "-c", "java -jar \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -DDATABASE_URL=$DATABASE_URL \
    -DDATABASE_USERNAME=$DATABASE_USERNAME \
    -DDATABASE_PASSWORD=$DATABASE_PASSWORD \
    app.jar"]