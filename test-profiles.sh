#!/bin/bash

echo "=== Spring Boot Profile 테스트 스크립트 ==="

profiles=("dev" "dev-embedded" "staging" "prod")

for profile in "${profiles[@]}"; do
    echo ""
    echo "🔄 Testing profile: $profile"
    echo "================================="
    
    # 환경변수 설정 (staging, prod용)
    if [[ "$profile" == "staging" || "$profile" == "prod" ]]; then
        export DB_HOST="localhost"
        export DB_PORT="5432"
        export DB_NAME="zzic_db"
        export DB_USERNAME="zzic_user"
        export DB_PASSWORD="zzic_pass"
        export REDIS_HOST="localhost"
        export REDIS_PORT="6379"
        export REDIS_PASSWORD=""
    fi
    
    # 애플리케이션 시작
    timeout 30s ./gradlew bootRun --args="--spring.profiles.active=$profile" > "profile_test_$profile.log" 2>&1 &
    APP_PID=$!
    
    # 애플리케이션 시작 대기
    sleep 15
    
    # 헬스체크
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ Profile $profile: SUCCESS - Application started and responding"
    else
        echo "❌ Profile $profile: FAILED - Application not responding"
        echo "   Check profile_test_$profile.log for details"
    fi
    
    # 애플리케이션 종료
    kill $APP_PID 2>/dev/null
    sleep 5
    
    # 환경변수 정리
    unset DB_HOST DB_PORT DB_NAME DB_USERNAME DB_PASSWORD REDIS_HOST REDIS_PORT REDIS_PASSWORD
done

echo ""
echo "✨ Profile 테스트 완료!"
echo "각 프로필의 상세 로그는 profile_test_[profile].log 파일을 확인하세요."
