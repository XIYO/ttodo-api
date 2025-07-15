#!/bin/bash

# 모든 테스트 파일 찾기 (BaseIntegrationTest 제외)
find src/test -name "*Test.java" -type f | grep -v BaseIntegrationTest | while read file; do
    # @SpringBootTest가 있는지 확인
    if grep -q "@SpringBootTest" "$file"; then
        echo "Updating: $file"
        
        # import 추가
        if ! grep -q "import point.ttodoApi.test.BaseIntegrationTest;" "$file"; then
            # package 선언 다음에 import 추가
            sed -i '' '/^package /a\
import point.ttodoApi.test.BaseIntegrationTest;
' "$file"
        fi
        
        # @SpringBootTest 제거하고 extends BaseIntegrationTest 추가
        # 클래스 선언 찾기
        if grep -q "class.*Test {" "$file"; then
            # @SpringBootTest 제거
            sed -i '' '/@SpringBootTest/d' "$file"
            # extends BaseIntegrationTest 추가
            sed -i '' 's/class \([^ ]*\) {/class \1 extends BaseIntegrationTest {/g' "$file"
        elif grep -q "class.*Test<.*> {" "$file"; then
            # 제네릭이 있는 경우
            sed -i '' '/@SpringBootTest/d' "$file"
            sed -i '' 's/class \([^ ]*\) {/class \1 extends BaseIntegrationTest {/g' "$file"
        fi
    fi
done

echo "Test files updated successfully!"