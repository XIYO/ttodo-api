# 애플리케이션 서비스 계층 검토 보고서

## 1. 전반적인 구조 분석

현재 애플리케이션은 전형적인 계층형 아키텍처(Layered Architecture)를 따르고 있으며, DDD의 일부 개념을 적용하고 있습니다.

### 주요 계층 구성
- **Presentation Layer**: Controller 클래스들
- **Application Layer**: Service 클래스들 (트랜잭션 스크립트 패턴)
- **Domain Layer**: Entity 클래스들 (빈약한 도메인 모델)
- **Infrastructure Layer**: Repository 구현체들

## 2. Application Service vs Domain Service 구분

### 현재 상태
- **도메인 서비스가 부재**: 모든 비즈니스 로직이 Application Service에 집중되어 있음
- **빈약한 도메인 모델**: Entity들이 단순한 데이터 컨테이너 역할만 수행
- **트랜잭션 스크립트 패턴**: 대부분의 로직이 Service 메서드에 절차적으로 구현됨

### 개선 방안
1. **도메인 서비스 도입이 필요한 영역**:
   - Todo 반복 일정 생성 로직 → `TodoRepeatService`
   - 경험치 계산 로직 → `ExperienceCalculationService`
   - Challenge 성공률 계산 → `ChallengeStatisticsService`

2. **도메인 모델 강화**:
   - `TodoOriginal.togglePin()` 처럼 도메인 객체가 자체 행위를 가지도록 개선
   - 불변식(invariant) 검증 로직을 도메인 객체로 이동

## 3. 트랜잭션 경계 설정

### 현재 상태
- 클래스 레벨 `@Transactional(readOnly = true)` 기본 설정
- 쓰기 작업에 메서드 레벨 `@Transactional` 적절히 적용
- 이벤트 핸들러에서의 트랜잭션 분리 미흡

### 문제점
1. **트랜잭션 범위가 너무 큼**: 단일 메서드에서 여러 집계를 수정
2. **이벤트 핸들러 트랜잭션**: `@EventListener` 사용으로 동일 트랜잭션에서 실행

### 개선 방안
```java
// 현재
@EventListener
public void handleTodoCompleted(TodoCompletedEvent event) {
    experienceService.addExperience(event.memberId(), 10);
}

// 개선 - 트랜잭션 분리
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleTodoCompleted(TodoCompletedEvent event) {
    experienceService.addExperience(event.memberId(), 10);
}
```

## 4. 도메인 로직과 애플리케이션 로직의 분리

### 현재 문제점

1. **TodoOriginalService의 도메인 로직 혼재**:
   ```java
   // 애플리케이션 로직과 도메인 로직이 혼재
   public void changeOrder(UUID memberId, Long todoId, Integer newOrder) {
       // 권한 검증 (애플리케이션 로직)
       TodoOriginal target = todoOriginalRepository.findByIdAndMemberId(todoId, memberId);
       
       // 비즈니스 규칙 검증 (도메인 로직)
       if (!target.getIsPinned()) {
           throw new IllegalArgumentException("Only pinned todos can be reordered");
       }
       
       // 순서 변경 로직 (도메인 로직)
       // ...
   }
   ```

2. **VirtualTodoService의 복잡한 비즈니스 로직**:
   - 600줄 이상의 거대한 서비스 클래스
   - 가상 투두 생성, 날짜 계산 등 도메인 로직이 서비스에 산재

### 개선 방안
```java
// 도메인 서비스로 분리
@Component
public class TodoOrderingService {
    public List<TodoOriginal> reorderPinnedTodos(
        List<TodoOriginal> pinnedTodos, 
        TodoOriginal target, 
        int newOrder
    ) {
        // 순수한 도메인 로직
    }
}

// 애플리케이션 서비스는 조율만 담당
@Transactional
public void changeOrder(UUID memberId, Long todoId, Integer newOrder) {
    TodoOriginal target = todoOriginalRepository.findByIdAndMemberId(todoId, memberId);
    List<TodoOriginal> pinnedTodos = todoOriginalRepository.findByMemberIdAndIsPinnedTrue(memberId);
    
    List<TodoOriginal> reordered = todoOrderingService.reorderPinnedTodos(
        pinnedTodos, target, newOrder
    );
    
    todoOriginalRepository.saveAll(reordered);
}
```

## 5. CQRS 패턴 적용

### 현재 상태
- Command와 Query DTO가 분리되어 있음
- 하지만 실제 처리는 동일한 서비스에서 수행

### 문제점
1. **명령과 조회가 혼재**: 같은 서비스 클래스에서 CUD와 R 작업 모두 처리
2. **복잡한 조회 로직**: VirtualTodoService의 조회 로직이 과도하게 복잡

### 개선 방안

#### 1. Command와 Query 서비스 분리
```java
// Command Service
@Service
@Transactional
public class TodoCommandService {
    public void createTodo(CreateTodoCommand command) { }
    public void updateTodo(UpdateTodoCommand command) { }
    public void deleteTodo(DeleteTodoCommand command) { }
}

// Query Service
@Service
@Transactional(readOnly = true)
public class TodoQueryService {
    public TodoResult getTodo(TodoQuery query) { }
    public Page<TodoResult> getTodoList(TodoSearchQuery query) { }
}
```

#### 2. 읽기 전용 모델 도입
```java
// 복잡한 조회를 위한 읽기 모델
@Entity
@Table(name = "todo_read_model")
public class TodoReadModel {
    private String id;
    private String title;
    private LocalDate date;
    private boolean isVirtual;
    private boolean isCompleted;
    // 조회에 최적화된 구조
}
```

## 6. 도메인 이벤트 활용

### 현재 상태
- 기본적인 이벤트 발행/구독 구조 구현
- `TodoCompletedEvent`, `TodoUncompletedEvent` 등 사용

### 문제점
1. **이벤트 발행 위치**: 애플리케이션 서비스에서 직접 발행
2. **도메인 이벤트 부재**: 도메인 객체가 이벤트를 발행하지 않음
3. **트랜잭션 경계**: 이벤트 핸들러가 동일 트랜잭션에서 실행

### 개선 방안

#### 1. 도메인 이벤트 발행
```java
@Entity
public class TodoOriginal extends AggregateRoot {
    public void complete() {
        if (this.complete) return;
        
        this.complete = true;
        // 도메인 객체에서 이벤트 발행
        registerEvent(new TodoCompletedEvent(
            this.member.getId(), 
            this.id, 
            this.title
        ));
    }
}
```

#### 2. 이벤트 스토어 패턴
```java
@Entity
@Table(name = "domain_events")
public class DomainEvent {
    @Id
    private String eventId;
    private String aggregateId;
    private String eventType;
    private String eventData;
    private Instant occurredOn;
    private boolean published;
}
```

## 7. 서비스 간 의존성 관리

### 현재 문제점
1. **순환 의존성 위험**: 서비스 간 직접 참조
2. **높은 결합도**: `TodoOriginalService`가 `MemberService`를 직접 의존
3. **테스트 어려움**: 의존성이 많아 단위 테스트 작성이 어려움

### 개선 방안

#### 1. 인터페이스 기반 의존성
```java
// 도메인 계층의 인터페이스
public interface MemberRepository {
    Optional<Member> findById(UUID id);
}

// 애플리케이션 서비스는 인터페이스에만 의존
@Service
public class TodoOriginalService {
    private final MemberRepository memberRepository; // 인터페이스 의존
}
```

#### 2. 이벤트 기반 통신
```java
// 직접 호출 대신 이벤트 발행
public void createTodo(CreateTodoCommand command) {
    // Todo 생성 로직
    
    // 직접 호출하지 않고 이벤트 발행
    eventPublisher.publishEvent(new TodoCreatedEvent(
        todo.getId(), 
        todo.getMember().getId()
    ));
}
```

## 8. 추천 리팩토링 우선순위

1. **긴급**:
   - VirtualTodoService 분해 (600줄 → 여러 작은 서비스로)
   - 도메인 로직을 Entity로 이동
   - 이벤트 핸들러 트랜잭션 분리

2. **중요**:
   - CQRS 패턴 적용 (Command/Query 서비스 분리)
   - 도메인 서비스 도입
   - 읽기 전용 모델 구축

3. **장기적**:
   - 이벤트 소싱 도입 검토
   - 마이크로서비스 분리 준비
   - 헥사고날 아키텍처로 전환

## 9. 결론

현재 애플리케이션은 기본적인 계층형 아키텍처는 갖추고 있으나, DDD 원칙의 적용이 미흡합니다. 특히 도메인 로직이 애플리케이션 서비스에 집중되어 있고, 복잡한 비즈니스 로직의 관리가 어려운 상황입니다.

단계적으로 도메인 모델을 강화하고, CQRS 패턴을 적용하며, 이벤트 기반 아키텍처로 전환하는 것을 추천합니다. 이를 통해 더 유지보수가 쉽고 확장 가능한 시스템으로 발전시킬 수 있을 것입니다.