# Board-hole 구조 리팩토링 가이드

## 🎯 개요

이 가이드는 ttodo-api를 board-hole과 동일한 DDD 구조로 리팩토링하는 과정을 설명합니다.

## 🛠️ 설정된 도구

### 1. OpenRewrite
- **파일**: `rewrite.yml`
- **목적**: 패키지 이동과 import 문 자동 업데이트
- **플러그인**: `org.openrewrite.rewrite` version 6.27.0

### 2. Gradle 커스텀 태스크
- **파일**: `gradle/refactoring.gradle`
- **목적**: 디렉토리 생성, 파일 물리적 이동, 검증

## 📋 리팩토링 실행 단계

### 1단계: 구조 리팩토링 (자동화)

```bash
# 전체 리팩토링 실행
./gradlew refactorToBoardHoleStructure
```

이 명령은 다음을 자동으로 수행합니다:
- ✅ board-hole 스타일 디렉토리 구조 생성
- ✅ 파일들을 적절한 위치로 이동
- ✅ 패키지 선언문 업데이트
- ✅ import 문 업데이트
- ✅ 빈 디렉토리 정리

### 2단계: OpenRewrite 추가 정리

```bash
# OpenRewrite로 누락된 부분 정리
./gradlew rewriteRun
```

### 3단계: 구조 검증

```bash
# board-hole 구조 준수 여부 검증
./gradlew validateBoardHoleStructure
```

### 4단계: 빌드 검증

```bash
# 빌드가 성공하는지 확인
./gradlew clean build
```

## 🏗️ 변경되는 구조

### Before (현재 ttodo-api)
```
domain/
├── application/
│   ├── dto/
│   │   ├── command/    ❌ 제거 예정
│   │   ├── query/      ❌ 제거 예정
│   │   └── result/     ❌ 제거 예정
│   └── [services]
├── presentation/
│   ├── dto/
│   │   ├── request/    ❌ 평탄화 예정
│   │   └── response/   ❌ 평탄화 예정
└── exception/          ❌ shared로 이동 예정
```

### After (board-hole 스타일)
```
domain/
├── application/
│   ├── command/        ✅ 명령 객체
│   ├── query/          ✅ 조회 객체
│   ├── event/          ✅ 도메인 이벤트
│   ├── result/         ✅ 결과 DTO
│   └── mapper/         ✅ 애플리케이션 매퍼
├── domain/
│   └── validation/
│       ├── required/   ✅ 필수 검증
│       └── optional/   ✅ 선택 검증
├── presentation/
│   ├── dto/           ✅ 모든 Request/Response (평탄화)
│   └── mapper/        ✅ Presentation 매퍼
└── infrastructure/    ✅ 구현체

shared/
├── exception/         ✅ 모든 예외 (도메인별 하위폴더)
├── config/           ✅ 모든 설정 (도메인별 하위폴더)
└── security/         ✅ 보안 관련
```

## 📝 개별 태스크 사용법

필요시 개별 단계만 실행할 수 있습니다:

```bash
# 1. 디렉토리 구조만 생성
./gradlew createBoardHoleStructure

# 2. 파일 이동만 수행
./gradlew moveFilesToBoardHoleStructure

# 3. 패키지 선언만 업데이트
./gradlew updatePackageDeclarations

# 4. Import 문만 업데이트
./gradlew updateImports

# 5. 빈 디렉토리만 정리
./gradlew cleanEmptyDirectories

# 6. 구조 검증
./gradlew validateBoardHoleStructure
```

## ⚠️ 주의사항

1. **Git 커밋**: 리팩토링 전에 현재 상태를 커밋하세요
2. **IDE 재시작**: 리팩토링 후 IntelliJ IDEA 재시작 권장
3. **Import 최적화**: IDE에서 "Optimize Imports" 실행 권장
4. **테스트**: 리팩토링 후 모든 테스트가 통과하는지 확인

## 🔍 검증 포인트

리팩토링이 올바르게 완료되었는지 확인하는 포인트들:

### ✅ 성공 지표
- `application` 레이어에 `dto` 디렉토리가 없음
- `presentation/dto`가 평탄화됨 (request/response 서브디렉토리 없음)
- 모든 예외가 `shared/exception/[도메인]/`으로 이동
- 모든 설정이 `shared/config/[도메인]/`으로 이동
- 빌드가 성공함 (`./gradlew clean build`)

### ❌ 실패 지표
- 컴파일 에러 발생
- Import 문에서 패키지를 찾지 못함
- `validateBoardHoleStructure` 태스크 실패

## 🎯 완료 후 혜택

1. **일관성**: board-hole과 동일한 구조로 팀 간 이해도 향상
2. **명확성**: 레이어별 책임 분리 명확화
3. **유지보수성**: 중앙화된 예외/설정 관리
4. **확장성**: 새 도메인 추가 시 명확한 가이드라인

## 📞 문제 해결

문제가 발생할 경우:

1. Git에서 이전 상태로 롤백
2. 개별 태스크로 단계별 실행
3. `validateBoardHoleStructure`로 현재 상태 확인
4. 로그를 확인하여 구체적인 오류 파악

이 리팩토링을 통해 ttodo-api가 board-hole과 동일한 깔끔한 DDD 구조를 갖게 됩니다! 🎉