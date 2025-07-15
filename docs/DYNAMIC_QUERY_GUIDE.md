# 동적 쿼리 시스템 사용 가이드

## 개요

ttodo-api는 JPA Criteria API 기반의 타입 안전 동적 쿼리 시스템을 제공합니다. 이 시스템은 SQL Injection을 방지하면서도 유연한 검색 기능을 제공합니다.

## 주요 컴포넌트

### 1. BaseSpecification
- 모든 도메인별 Specification의 기반 클래스
- 공통 정렬 필드와 SQL Injection 방지 로직 제공
- 화이트리스트 기반 필드 검증

### 2. SpecificationBuilder
- 유창한 API(Fluent API)를 통한 동적 쿼리 구성
- 다양한 조건 연산자 지원 (equals, like, in, between 등)
- 조건부 쿼리 추가 기능

### 3. SortValidator
- 정렬 필드 화이트리스트 검증
- SQL Injection 방지를 위한 패턴 검증
- 안전한 정렬 필드 sanitization

### 4. IndexAdvisor
- 쿼리 사용 패턴 분석
- 인덱스 생성 추천
- 성능 최적화 가이드 제공

## 사용 예제

### 1. 기본 검색 쿼리

```java
@Service
public class TodoSearchService {
    
    public Page<Todo> searchTodos(TodoSearchRequest request, Pageable pageable) {
        // 정렬 필드 검증
        sortValidator.validateSort(pageable.getSort(), todoSpecification);
        
        // 동적 쿼리 구성
        SpecificationBuilder<Todo> builder = new SpecificationBuilder<>(todoSpecification);
        
        Specification<Todo> spec = builder
                .with("owner.id", request.getOwnerId())
                .with("active", true)
                .withLike("title", request.getKeyword())
                .withIn("category.id", request.getCategoryIds())
                .withDateRange("date", request.getStartDate(), request.getEndDate())
                .build();
        
        return todoRepository.findAll(spec, pageable);
    }
}
```

### 2. 조건부 쿼리

```java
Specification<Todo> spec = builder
    .with("owner.id", ownerId)
    .withIf(request.getComplete() != null, "complete", request.getComplete())
    .withIf(request.isUrgentOnly(), builder2 -> 
        builder2.with("priorityId", 1)
               .withBetween("date", LocalDate.now(), LocalDate.now().plusDays(3)))
    .build();
```

### 3. 복잡한 OR 조건

```java
Specification<Member> spec = builder
    .with("active", true)
    .or(builder2 -> builder2
        .withLike("email", keyword)
        .withLike("nickname", keyword))
    .build();
```

## 도메인별 Specification 구현

### 1. TodoSpecification

```java
@Component
public class TodoSpecificationV2 extends BaseSpecification<Todo> {
    private static final Set<String> TODO_SORT_FIELDS = Set.of(
        "title", "complete", "date", "priorityId", "orderIndex"
    );
    
    @Override
    protected Set<String> getAllowedSortFields() {
        Set<String> allowedFields = new HashSet<>(COMMON_SORT_FIELDS);
        allowedFields.addAll(TODO_SORT_FIELDS);
        return allowedFields;
    }
}
```

### 2. 검색 서비스 구현

```java
@Service
@RequiredArgsConstructor
public class TodoSearchService {
    private final TodoRepository todoRepository;
    private final TodoSpecificationV2 todoSpecification;
    private final SortValidator sortValidator;
    
    public Page<Todo> searchTodos(TodoSearchRequest request, Pageable pageable) {
        // 구현...
    }
}
```

## 보안 고려사항

### 1. SQL Injection 방지

- 모든 필드명은 화이트리스트 검증
- 정규표현식 패턴으로 필드명 검증
- Criteria API의 타입 안전성 활용

### 2. 정렬 필드 검증

```java
// SortValidator가 자동으로 검증
sortValidator.validateSort(pageable.getSort(), specification);
```

### 3. 안전한 필드명 패턴

```java
// 허용되는 필드명 패턴
^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*$

// 예시
"title" ✓
"owner.id" ✓
"category.name" ✓
"drop table users" ✗
```

## 성능 최적화

### 1. 인덱스 추천 활용

```java
// IndexAdvisor를 통한 인덱스 추천 확인
List<IndexRecommendation> recommendations = indexAdvisor.getIndexRecommendations();
List<String> indexSql = indexAdvisor.generateIndexSql();
```

### 2. 쿼리 성능 모니터링

- SpecificationPerformanceAspect가 자동으로 성능 측정
- 1초 이상 소요되는 느린 쿼리 자동 경고
- 쿼리 사용 패턴 분석

### 3. 추천 인덱스 예시

```sql
-- Recommended: 150 executions, avg 75.32ms
CREATE INDEX idx_todo_owner_id_active ON todo (owner_id, active);

-- Recommended: 89 executions, avg 120.45ms
CREATE INDEX idx_todo_category_id_complete ON todo (category_id, complete);
```

## API 엔드포인트 예제

### 1. Todo 검색 API

```http
GET /api/search/todos?keyword=회의&complete=false&categoryIds=1,2,3
```

### 2. Member 검색 API (관리자용)

```http
GET /api/search/members?emailKeyword=@example.com&role=USER&recentlyActiveOnly=true
```

### 3. Challenge 검색 API

```http
GET /api/search/challenges?visibility=PUBLIC&ongoingOnly=true&joinableOnly=true
```

## 확장 가이드

### 1. 새로운 도메인 Specification 추가

```java
@Component
public class NewEntitySpecification extends BaseSpecification<NewEntity> {
    private static final Set<String> ENTITY_SORT_FIELDS = Set.of(
        // 엔티티별 정렬 가능 필드 정의
    );
    
    @Override
    protected Set<String> getAllowedSortFields() {
        Set<String> allowedFields = new HashSet<>(COMMON_SORT_FIELDS);
        allowedFields.addAll(ENTITY_SORT_FIELDS);
        return allowedFields;
    }
}
```

### 2. 커스텀 검색 조건 추가

```java
public class CustomSpecificationBuilder<T> extends SpecificationBuilder<T> {
    public CustomSpecificationBuilder<T> withCustomCondition(String field, Object value) {
        // 커스텀 조건 로직 구현
        return this;
    }
}
```

## 테스트 가이드

### 1. 단위 테스트

```java
@Test
void testDynamicQuery() {
    // Given
    TodoSearchRequest request = new TodoSearchRequest();
    request.setKeyword("테스트");
    request.setComplete(false);
    
    // When
    Page<Todo> result = todoSearchService.searchTodos(request, PageRequest.of(0, 10));
    
    // Then
    assertThat(result).isNotEmpty();
    assertThat(result.getContent()).allMatch(todo -> 
        todo.getTitle().contains("테스트") && !todo.isComplete()
    );
}
```

### 2. 통합 테스트

```java
@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {
    
    @Test
    void testSearchEndpoint() throws Exception {
        mockMvc.perform(get("/api/search/todos")
                .param("keyword", "회의")
                .param("complete", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
```

## 주의사항

1. **대량 데이터 처리**: 페이징을 항상 사용하여 메모리 문제 방지
2. **N+1 문제**: 연관 엔티티 조회 시 fetch join 고려
3. **인덱스 전략**: IndexAdvisor 추천을 참고하여 적절한 인덱스 생성
4. **정렬 필드**: 반드시 화이트리스트에 등록된 필드만 사용

## 문제 해결

### 1. "Invalid sort field" 오류

- 해당 필드가 Specification의 getAllowedSortFields()에 포함되어 있는지 확인
- 필드명이 올바른 패턴을 따르는지 확인

### 2. 느린 쿼리 성능

- IndexAdvisor 추천 확인
- 적절한 인덱스 생성
- 쿼리 조건 최적화

### 3. SQL Injection 시도 감지

- 로그 확인 및 보안팀 알림
- 해당 요청 차단 및 분석