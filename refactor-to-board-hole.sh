#!/bin/bash

# Board-hole 구조 리팩토링 자동화 스크립트

echo "🚀 Board-hole 구조 리팩토링 시작!"
echo "======================================"

# 현재 git 상태 확인
if [[ -n $(git status --porcelain) ]]; then
    echo "⚠️  경고: 커밋되지 않은 변경사항이 있습니다."
    echo "   계속하시겠습니까? (y/N)"
    read -r answer
    if [[ ! $answer =~ ^[Yy]$ ]]; then
        echo "❌ 리팩토링이 취소되었습니다."
        exit 1
    fi
fi

# 1단계: 구조 리팩토링
echo ""
echo "📁 1단계: Board-hole 구조 리팩토링 실행..."
if ./gradlew refactorToBoardHoleStructure; then
    echo "✅ 구조 리팩토링 완료!"
else
    echo "❌ 구조 리팩토링 실패!"
    exit 1
fi

# 2단계: OpenRewrite 추가 정리
echo ""
echo "🔧 2단계: OpenRewrite로 추가 정리..."
if ./gradlew rewriteRun; then
    echo "✅ OpenRewrite 정리 완료!"
else
    echo "⚠️  OpenRewrite 정리 중 오류가 있을 수 있지만 계속 진행합니다."
fi

# 3단계: 구조 검증
echo ""
echo "🔍 3단계: 구조 검증..."
if ./gradlew validateBoardHoleStructure; then
    echo "✅ 구조 검증 통과!"
else
    echo "❌ 구조 검증 실패! 수동으로 확인이 필요합니다."
fi

# 4단계: 빌드 검증
echo ""
echo "🏗️  4단계: 빌드 검증..."
if ./gradlew clean build -x test; then
    echo "✅ 빌드 성공!"
else
    echo "❌ 빌드 실패! import 문이나 패키지 경로를 확인하세요."
    echo ""
    echo "🔧 해결 방법:"
    echo "1. IntelliJ IDEA 재시작"
    echo "2. 'Optimize Imports' 실행"
    echo "3. 수동으로 남은 import 문 수정"
    exit 1
fi

# 완료
echo ""
echo "🎉 Board-hole 구조 리팩토링 완료!"
echo "======================================"
echo ""
echo "✅ 완료된 작업:"
echo "  - 디렉토리 구조 board-hole 스타일로 변경"
echo "  - application/dto → application/command,query,result 분리"
echo "  - presentation/dto 평탄화 (request/response 제거)"
echo "  - exception을 shared/exception으로 중앙화"
echo "  - config를 shared/config로 중앙화"
echo "  - 패키지 선언 및 import 문 업데이트"
echo ""
echo "📋 다음 단계:"
echo "1. IntelliJ IDEA 재시작"
echo "2. 'Optimize Imports' 실행 (Ctrl+Alt+O)"
echo "3. ./gradlew test 로 테스트 실행"
echo ""
echo "🎯 이제 ttodo-api가 board-hole과 동일한 구조를 가집니다!"