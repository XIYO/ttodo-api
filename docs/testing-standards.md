# 테스트 코드 작성 가이드

TTODO-API 프로젝트의 테스트 코드 작성 표준 가이드입니다.

## 📋 목차
1. [메서드 네이밍 규칙](#1-메서드-네이밍-규칙)
2. [DisplayName 사용 기준](#2-displayname-사용-기준)
3. [테스트 구조 패턴](#3-테스트-구조-패턴)
4. [복잡도 기반 구조 선택](#4-복잡도-기반-구조-선택)
5. [IDE 리팩토링 가이드](#5-ide-리팩토링-가이드)
6. [검증 스크립트](#6-검증-스크립트)

---

## 1. 메서드 네이밍 규칙

### ✅ 표준 패턴
```
<method>_<result>_<condition>
```

### 📌 예시
**좋은 예**:
```java
@Test
void createTodo_Success_WithAllFields() { ... }

@Test
void updateUser_Failure_NotFound() { ... }

@Test
void signUp_ThrowsException_WhenDuplicateEmail() { ... }
```

**나쁜 예**:
```java
@Test
void testCreateTodo() { ... }  // ❌ test 접두어 금지

@Test
void shouldCreateTodo() { ... }  // ❌ should 패턴 혼용 방지

@Test
void createTodoTest() { ... }  // ❌ Test 접미어 금지
```

### 🎯 Result 키워드
- `Success` - 정상 동작
- `Failure` - 비즈니스 실패 (400, 404 등)
- `ThrowsException` - 예외 발생
- `Returns` - 특정 값 반환 (도메인 테스트)

### 📝 예외 허용 케이스
- **통합 테스트**: 시나리오명 사용 가능
  ```java
  void fullAuthenticationFlow() { ... }
  void endToEndTodoCreationAndCompletion() { ... }
  ```
- **성능 테스트**: benchmark 접두어 허용
  ```java
  void benchmarkTodoCreationWith1000Items() { ... }
  ```

---

## 2. DisplayName 사용 기준

### ✅ 필수 사용
1. **모든 @Nested 클래스**
   ```java
   @Nested
   @DisplayName("1. CREATE - TODO 생성")
   class CreateTests { ... }
   ```

2. **모든 테스트 메서드**
   ```java
   @Test
   @DisplayName("TODO 생성 성공 - 유효한 데이터")
   void createTodo_Success_WithValidData() { ... }
   ```

### 🔄 선택 사용 (생략 가능)
- 메서드명이 충분히 명확한 **단순 도메인 테스트**
- 테스트 클래스에 5개 이하 메서드만 있을 때

```java
// 생략 가능 예시
@Test
void add_ReturnsSum() {
    assertThat(calculator.add(2, 3)).isEqualTo(5);
}
```

### 📖 작성 가이드
- **언어**: 한글 우선, 도메인 용어는 영문 혼용 가능
- **형식**: "동작 설명 - 조건/결과"
- **길이**: 한 줄 내외 (50자 이하 권장)

```java
@DisplayName("사용자 생성 성공 - 모든 필수 필드 포함")  // ✅
@DisplayName("사용자 생성 실패 - 이메일 중복")  // ✅
@DisplayName("사용자를 생성할 때 모든 필수 필드가 있으면 성공한다")  // ❌ 너무 장황
```

---

## 3. 테스트 구조 패턴

### 🌐 웹 계층 (@WebMvcTest)
**패턴**: CRUD 순서 → Nested 성공/실패/엣지

```java
@WebMvcTest(TodoController.class)
@DisplayName("TodoController 단위 테스트")
class TodoControllerTest {

    @Nested
    @DisplayName("1. CREATE - TODO 생성")
    class CreateTests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("TODO 생성 성공 - 필수 필드만")
            void createTodo_Success_RequiredFieldsOnly() { ... }
        }

        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            @Test
            @DisplayName("TODO 생성 실패 - 제목 미입력")
            void createTodo_Failure_NoTitle() { ... }
        }

        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            @Test
            @DisplayName("TODO 생성 - 설명 미입력시 기본값 사용")
            void createTodo_EdgeCase_NoDescription() { ... }
        }
    }

    @Nested
    @DisplayName("2. READ - TODO 조회")
    class ReadTests { ... }

    @Nested
    @DisplayName("3. UPDATE - TODO 수정")
    class UpdateTests { ... }

    @Nested
    @DisplayName("4. DELETE - TODO 삭제")
    class DeleteTests { ... }
}
```

**CRUD 순서 규칙**:
1. CREATE
2. READ
3. UPDATE
4. DELETE

특수 케이스: 조회가 많으면 0. SEARCH 추가 가능

### 🏛️ 도메인 계층 (순수 단위 테스트)
**패턴**: 복잡도 기반 선택

#### 복잡도 낮음 (5개 이하) - 평면 구조
```java
@DisplayName("RecurrenceRule 생성 테스트")
class RecurrenceRuleTest {

    @Test
    @DisplayName("일일 반복 규칙 생성 성공")
    void generateRecurrence_Success_WithDailyRule() { ... }

    @Test
    @DisplayName("주간 반복 규칙 생성 성공")
    void generateRecurrence_Success_WithWeeklyRule() { ... }

    @Test
    @DisplayName("월간 반복 규칙 생성 성공")
    void generateRecurrence_Success_WithMonthlyRule() { ... }
}
```

#### 복잡도 높음 (6개 이상) - Nested 구조
```java
@DisplayName("ByRulesValidator 검증 테스트")
class ByRulesValidatorTest {

    @Nested
    @DisplayName("BYWEEKDAY 검증")
    class ValidateByWeekDay {

        @Test
        @DisplayName("유효한 요일로 성공")
        void validate_Success_WithValidWeekday() { ... }

        @Test
        @DisplayName("잘못된 요일로 실패")
        void validate_Failure_WithInvalidWeekday() { ... }
    }

    @Nested
    @DisplayName("BYMONTHDAY 검증")
    class ValidateByMonthDay {
        @Test
        @DisplayName("1-31일 범위 검증 성공")
        void validate_Success_WithValidDay() { ... }
    }
}
```

### 🔧 인프라 계층
도메인 계층과 동일 (복잡도 기반)

---

## 4. 복잡도 기반 구조 선택

### 📊 판단 기준

| 테스트 수 | 구조 | 예시 |
|----------|------|------|
| 1-5개 | 평면 구조 | `RecurrenceRuleTest` |
| 6-15개 | 1단 Nested (기능별) | `ByRulesValidatorTest` |
| 16개 이상 | 2단 Nested (기능→성공/실패) | `TodoControllerTest` |

### 🎯 Nested 사용 시점
다음 중 하나라도 해당하면 Nested 권장:
- ✅ 테스트 메서드 6개 이상
- ✅ 명확한 기능별 그룹이 존재 (예: BYWEEKDAY, BYMONTHDAY)
- ✅ 성공/실패 케이스 분리가 필요한 경우

### ⚠️ 과도한 중첩 방지
- 3단 이상 중첩 지양
- "성공/실패" 분리가 불필요하면 생략
- 테스트 1-2개짜리 Nested 클래스 지양

```java
// ❌ 나쁜 예: 과도한 중첩
@Nested
class Feature {
    @Nested
    class SubFeature {
        @Nested
        class Success {
            @Nested
            class EdgeCase {  // 4단 중첩
                @Test
                void test() { ... }
            }
        }
    }
}

// ✅ 좋은 예: 적절한 중첩
@Nested
class Feature {
    @Test
    void feature_Success_EdgeCase() { ... }
}
```

---

## 5. IDE 리팩토링 가이드

### IntelliJ IDEA

#### 단일 메서드 이름 변경
1. 메서드명에 커서 위치
2. `Shift + F6` (Refactor → Rename)
3. 새 이름 입력 후 Enter

#### 일괄 변경 (Regex)
1. `Ctrl + Shift + R` (Replace in Files)
2. Regex 옵션 활성화
3. 패턴 예시:

**test 접두어 제거**:
```regex
Find:    void test(\w+)\(
Replace: void $1_Success(
```

**카멜 → 스네이크**:
```regex
Find:    void (\w+)Test\(
Replace: void $1_Success(
```

#### @DisplayName 일괄 추가
1. 구조 검색 (Structural Search)
2. Template:
```java
@Test
$MethodName$() { }
```
3. Replace Template:
```java
@Test
@DisplayName("TODO")
$MethodName$() { }
```

### Visual Studio Code
```bash
# Regex 찾기/바꾸기
Ctrl + H → Use Regular Expression 체크
```

---

## 6. 검증 스크립트

### 자동 검증 명령어

#### test 접두어 잔존 확인
```bash
rg 'void test[A-Z]' src/test --count-matches
# 결과: 0이어야 함
```

#### 카멜 케이스 테스트 확인
```bash
rg 'void [a-z]+Test\(' src/test --count-matches
# 결과: 0이어야 함
```

#### DisplayName 누락 확인
```bash
rg '@Test' -A 1 src/test | rg -v '@DisplayName|@ParameterizedTest' | wc -l
# 결과: 낮을수록 좋음 (단순 도메인 테스트만 허용)
```

#### Nested 구조 확인
```bash
rg '@Nested' src/test --count-matches
# 결과: 예상 개수와 일치하는지 확인
```

#### 전체 테스트 통과 확인
```bash
./gradlew clean test
# 결과: BUILD SUCCESSFUL, 100% 통과
```

### 수동 체크리스트

실행 후 다음 항목을 수동 확인:

```
[ ] 모든 컨트롤러 테스트가 CRUD 순서를 준수하는가?
[ ] AuthController의 Simple/Security 테스트가 통합되었는가?
[ ] 도메인 테스트가 복잡도 기준을 따르는가? (5개 기준)
[ ] 모든 @Nested 클래스에 @DisplayName이 있는가?
[ ] 테스트 메서드명이 <method>_<result>_<condition> 패턴인가?
[ ] 3단 이상 중첩된 Nested가 없는가?
[ ] ./gradlew test 결과 100% 통과인가?
```

---

## 📚 참고 자료

### 관련 문서
- [CLAUDE.md](../CLAUDE.md) - 프로젝트 전체 가이드
- [Testing Patterns](https://github.com/junit-team/junit5/wiki/User-Guide) - JUnit 5 공식 가이드

### 예시 코드
- **좋은 예시**: `src/test/java/point/ttodoApi/todo/presentation/TodoControllerTest.java`
- **컨트롤러 구조**: `src/test/java/point/ttodoApi/auth/presentation/AuthControllerTest.java`

### 도구
- **검색**: ripgrep (`rg` 명령어)
- **리팩토링**: IntelliJ IDEA 구조 검색/바꾸기
- **테스트 실행**: `./gradlew test --tests "*ControllerTest"`

---

**최종 업데이트**: 2025-10-02
**버전**: 1.0.0
**작성자**: TTODO-API 프로젝트
