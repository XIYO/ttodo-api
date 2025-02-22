#!/bin/bash

# 1. 환경 변수 설정 (사용자가 직접 지정 가능)
IMAGE_NAME=${IMAGE_NAME:-""}  # 기본값 없음 (자동 추출)
IMAGE_TAG=${IMAGE_TAG:-""}    # 기본값 없음 (자동 추출)
CACHE_DIR="/tmp/.buildx-cache" # 빌드 캐시 저장 경로

# 2. Gradle 빌드 실행
echo "🛠️  Gradle 프로젝트 빌드 시작..."
./gradlew build --no-daemon

# 3. 실행 가능한 JAR 파일 찾기 (`-plain.jar` 제외)
JAR_FILE=$(ls -t build/libs/*.jar | grep -v 'plain' | head -n 1)

# 4. JAR 파일 존재 여부 확인
if [[ -z "$JAR_FILE" ]]; then
    echo "❌ 실행 가능한 JAR 파일을 찾을 수 없습니다."
    exit 1
fi

# 5. JAR 파일명에서 앱 이름과 버전 정보 추출
JAR_BASENAME=$(basename "$JAR_FILE")

# 6. 정규식을 이용해 이미지명과 태그 추출
if [[ $JAR_BASENAME =~ ^([a-zA-Z0-9-]+)-([0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?)\.jar$ ]]; then
    EXTRACTED_NAME="${BASH_REMATCH[1]}"  # 예: zzic-api
    EXTRACTED_TAG="${BASH_REMATCH[2]}"   # 예: 1.0.0-SNAPSHOT
else
    echo "⚠️  JAR 파일명에서 앱 이름과 버전을 추출할 수 없습니다."
    EXTRACTED_NAME="my-app"
    EXTRACTED_TAG="latest"
fi

# 7. 사용자가 환경 변수로 지정한 값이 없으면, 추출된 값을 사용
IMAGE_NAME=${IMAGE_NAME:-$EXTRACTED_NAME}
IMAGE_TAG=${IMAGE_TAG:-$EXTRACTED_TAG}

# 8. Docker 빌드 컨텍스트 설정
echo "📂 JAR 파일을 Docker 컨텍스트로 복사"
cp "$JAR_FILE" .

# 9. Docker Buildx 인스턴스가 없으면 생성
if ! docker buildx inspect multiarch-builder > /dev/null 2>&1; then
    echo "🔧 Docker Buildx 인스턴스 생성..."
    docker buildx create --name multiarch-builder --use
fi

# 10. Docker 빌드 실행 (캐시 활용)
echo "🐳 Docker 이미지 빌드 시작: ${IMAGE_NAME}:${IMAGE_TAG}"
docker buildx build \
    --platform linux/amd64,linux/arm64 \
    --build-arg JAR_FILE="$JAR_BASENAME" \
    --cache-from=type=local,src=$CACHE_DIR \
    --cache-to=type=local,dest=$CACHE_DIR \
    --load -t "${IMAGE_NAME}:${IMAGE_TAG}" .

# 11. 빌드 후 Docker 컨텍스트에서 JAR 파일 삭제 (깨끗한 환경 유지)
rm "$JAR_BASENAME"

# 12. 빌드 완료 메시지
echo "✅ Docker 이미지 빌드 완료: ${IMAGE_NAME}:${IMAGE_TAG}"