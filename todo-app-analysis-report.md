# Todo 애플리케이션 내부 분석 보고서

## 개요

본 보고서는 ZZIC-api 프로젝트의 Todo 애플리케이션 구현을 심층 분석하여 내부적인 문제점과 개선 방안을 제시합니다. 분석은 아키텍처, 코드 품질, 성능, 보안, 유지보수성 등 다양한 관점에서 수행되었습니다.

## 주요 발견사항 요약

### 🔴 심각한 문제
1. **복잡한 이중 엔티티 구조** (TodoOriginal vs Todo)
2. **서비스 레이어의 과도한 책임**
3. **성능 최적화 부재** (N+1 쿼리, 메모리 과다 사용)
4. **보안 취약점** (입력 검증 부족, Mass Assignment)

### 🟡 중요한 문제
1. **빈약한 도메인 모델** (Anemic Domain Model)
2. **높은 결합도와 낮은 응집도**
3. **일관성 없는 API 설계**
4. **테스트 어려움**

## 상세 분석

### 1. 아키텍처 문제점

#### 1.1 가상 Todo 설계 패턴 문제

**현재 구조:**
```
TodoOriginal (템플릿/반복 Todo)
    ↓
Todo (실제 인스턴스)
    - TodoId (originalTodoId + daysDifference)
```

**문제점:**
- `TodoOriginal`과 `Todo` 간의 복잡한 관계로 인한 코드 복잡도 증가
- 데이터 접근 시 두 테이블을 모두 확인해야 하는 비효율성
- `TodoId` 복합키 사용으로 인한 취약한 설계

**영향:**
- 쿼리 성능 저하
- 비즈니스 로직의 복잡도 증가
- 유지보수 어려움

#### 1.2 서비스 레이어 결합도

```java
// VirtualTodoService가 TodoOriginalService에 의존
public class VirtualTodoService {
    private final TodoOriginalService todoOriginalService;
    // 순환 참조 위험
}
```

### 2. 코드 품질 문제

#### 2.1 엔티티 설계 문제

**TodoOriginal.java의 문제점:**
```java
@Entity
@Getter
@Setter  // ❌ 캡슐화 위반
public class TodoOriginal {
    @ElementCollection(fetch = FetchType.EAGER)  // ❌ N+1 쿼리 문제
    private Set<String> tags = new HashSet<>();
    
    // ❌ 도메인 검증 로직 부재
    private LocalDate startDate;
    private LocalDate endDate;
}
```

**Todo.java의 문제점:**
```java
@Entity
@Getter
@Setter  // ❌ 캡슐화 위반
public class Todo {
    // ❌ TodoOriginal의 필드 중복
    private String title;
    private String description;
    // ...
}
```

#### 2.2 서비스 레이어 문제

**VirtualTodoService.java:**
- 600줄 이상의 거대 클래스 (단일 책임 원칙 위반)
- 복잡한 중첩 로직
- 하드코딩된 비즈니스 규칙

```java
// 복잡도가 높은 메서드 예시
private List<VirtualTodo> generateWeeklyVirtualDates(TodoOriginal todoOriginal, 
                                                    LocalDate today, 
                                                    int beforeDays, 
                                                    int afterDays) {
    // 200줄 이상의 복잡한 로직
}
```

### 3. 설계 패턴 위반

#### 3.1 빈약한 도메인 모델 (Anemic Domain Model)

```java
// ❌ 나쁜 예: 현재 구현
public class TodoOriginal {
    private boolean complete;
    // getter/setter만 존재, 비즈니스 로직 없음
}

// ✅ 좋은 예: 개선안
public class TodoOriginal {
    private boolean complete;
    
    public void markAsComplete() {
        if (this.complete) {
            throw new IllegalStateException("이미 완료된 할 일입니다.");
        }
        this.complete = true;
        // 도메인 이벤트 발행
    }
}
```

#### 3.2 DRY 원칙 위반

완료/미완료 상태 변경 로직이 여러 곳에 중복:
- `VirtualTodoService.updateTodo()`
- `VirtualTodoService.patchTodo()`
- `TodoOriginalService` 내 여러 메서드

### 4. 성능 문제

#### 4.1 데이터베이스 쿼리 문제

**N+1 쿼리 문제:**
```java
@ElementCollection(fetch = FetchType.EAGER)
private Set<String> tags = new HashSet<>();
// 각 TodoOriginal 조회 시 추가 쿼리 발생
```

**비효율적인 쿼리:**
```java
// 메모리에서 필터링
List<Todo> todos = todoRepository.findAll();
return todos.stream()
    .filter(/* 복잡한 조건 */)
    .collect(Collectors.toList());
```

#### 4.2 메모리 사용 문제

- 전체 Todo 목록을 메모리에 로드
- 대량의 가상 Todo 생성 시 메모리 부족 위험
- 캐싱 전략 부재

### 5. 보안 취약점

#### 5.1 입력 검증 부족

```java
// ❌ 검증 없는 날짜 범위
public List<VirtualTodo> getTodos(LocalDate startDate, LocalDate endDate) {
    // startDate와 endDate 간격 제한 없음 → DoS 공격 가능
}
```

#### 5.2 Mass Assignment 취약점

```java
@Setter  // ❌ 모든 필드 변경 가능
public class TodoOriginal {
    private Long memberId;  // 다른 사용자의 ID로 변경 가능
}
```

### 6. 유지보수성 문제

#### 6.1 높은 순환 복잡도

```java
// McCabe 복잡도 > 20
private List<VirtualTodo> generateVirtualTodos(...) {
    if (condition1) {
        if (condition2) {
            for (...) {
                if (condition3) {
                    // 깊은 중첩
                }
            }
        }
    }
}
```

#### 6.2 테스트 어려움

- 서비스 간 높은 결합도로 인한 단위 테스트 작성 어려움
- 복잡한 상태 관리로 인한 테스트 시나리오 증가
- Mock 객체 생성의 복잡성

### 7. 잠재적 버그

#### 7.1 동시성 문제

```java
// ❌ 낙관적 잠금 없음
@Entity
public class TodoOriginal {
    // @Version 어노테이션 누락
}
```

#### 7.2 상태 불일치

```java
// active와 complete 플래그의 모순 가능
todo.setActive(false);
todo.setComplete(true);  // 비활성화된 Todo가 완료됨?
```

#### 7.3 날짜 계산 오류

```java
// 복잡한 날짜 연산 시 검증 부족
int daysDifference = Period.between(startDate, targetDate).getDays();
// 월/년 경계를 넘어가는 경우 오류 가능성
```

### 8. API 설계 문제

#### 8.1 RESTful 설계 위반

```
❌ GET /todos/{id}:{daysDifference}  // 비표준 URL 패턴
✅ GET /todos/{originalId}/instances/{daysDifference}  // 개선안
```

#### 8.2 일관성 없는 응답 구조

```java
// 다양한 예외 처리 방식
throw new EntityNotFoundException("Todo not found");  // 일반적 예외
throw new RuntimeException("Invalid date");  // 구체적이지 않은 예외
```

## 개선 방안

### 1. 도메인 모델 리팩토링

#### 1.1 단일 엔티티로 통합

```java
@Entity
public class Todo {
    @Enumerated(EnumType.STRING)
    private TodoType type;  // SINGLE, RECURRING
    
    @Embedded
    private RecurrencePattern recurrence;  // Value Object
    
    // 비즈니스 로직 포함
    public void complete() {
        validateCanComplete();
        this.completedAt = LocalDateTime.now();
        Events.raise(new TodoCompletedEvent(this));
    }
}
```

#### 1.2 Value Object 도입

```java
@Embeddable
public class RecurrencePattern {
    private RepeatType type;
    private Set<DayOfWeek> daysOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 불변 객체로 구현
}
```

### 2. 서비스 레이어 개선

#### 2.1 책임 분리

```java
// Command/Query 분리
public class TodoCommandService {
    public void createTodo(CreateTodoCommand command) { }
    public void completeTodo(CompleteTodoCommand command) { }
}

public class TodoQueryService {
    public Page<TodoDto> findTodos(TodoSearchCriteria criteria) { }
}
```

#### 2.2 도메인 이벤트 활용

```java
@EventListener
public class TodoEventHandler {
    public void handle(TodoCompletedEvent event) {
        // 경험치 증가 등 부수 효과 처리
    }
}
```

### 3. 성능 최적화

#### 3.1 쿼리 최적화

```java
@Query("SELECT t FROM Todo t " +
       "LEFT JOIN FETCH t.tags " +
       "WHERE t.member.id = :memberId")
List<Todo> findByMemberIdWithTags(@Param("memberId") Long memberId);
```

#### 3.2 캐싱 전략

```java
@Cacheable(value = "todos", key = "#memberId")
public List<TodoDto> getTodosByMember(Long memberId) {
    // Redis 캐싱 활용
}
```

### 4. 보안 강화

#### 4.1 입력 검증

```java
@RestController
public class TodoController {
    @PostMapping("/todos")
    public ResponseEntity<TodoDto> createTodo(
        @Valid @RequestBody CreateTodoRequest request) {
        // Bean Validation 활용
    }
}
```

#### 4.2 불변 DTO

```java
@Value  // Lombok의 불변 객체
public class CreateTodoRequest {
    @NotBlank String title;
    @Size(max = 500) String description;
    @Future LocalDate dueDate;
}
```

### 5. 테스트 개선

```java
@Test
void 할일_완료시_경험치_이벤트_발행() {
    // Given
    Todo todo = TodoFixture.createPendingTodo();
    
    // When
    todo.complete();
    
    // Then
    verify(eventPublisher).publish(any(TodoCompletedEvent.class));
}
```

## 결론

현재 Todo 애플리케이션은 기능적으로는 동작하지만, 여러 아키텍처적 문제와 코드 품질 이슈를 가지고 있습니다. 특히 복잡한 이중 엔티티 구조와 서비스 레이어의 과도한 책임은 시급히 개선이 필요합니다.

### 우선순위별 개선 과제

1. **긴급 (1-2주)**
   - 보안 취약점 수정 (입력 검증, Mass Assignment)
   - 동시성 문제 해결 (낙관적 잠금 추가)

2. **단기 (1-2개월)**
   - 도메인 모델 리팩토링
   - 서비스 레이어 책임 분리
   - 성능 최적화 (쿼리 개선, 캐싱)

3. **장기 (3-6개월)**
   - 전체 아키텍처 재설계
   - 이벤트 기반 아키텍처 도입
   - 포괄적인 테스트 커버리지 확보

이러한 개선을 통해 더 견고하고 유지보수가 용이한 Todo 애플리케이션을 구축할 수 있을 것입니다.