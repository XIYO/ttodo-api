# 페이지네이션 및 검색 파라미터 검증 가이드

## 개요

ttodo-api는 모든 목록 조회 API에 대해 일관된 페이지네이션과 검색 파라미터 검증을 제공합니다. 이를 통해 안전하고 효율적인 데이터 조회가 가능합니다.

## 주요 컴포넌트

### 1. PageableValidator
- 페이지 번호, 크기, 정렬 필드의 유효성 검증
- SQL Injection 방지를 위한 패턴 검증
- 도메인별 허용된 정렬 필드 화이트리스트 검증

### 2. @ValidPageable 어노테이션
- 메서드 레벨에 적용하여 자동 Pageable 검증
- 도메인별 정렬 필드 제공자 지정

### 3. BaseSearchRequest
- 모든 검색 요청 DTO의 기본 클래스
- 페이징, 정렬 파라미터 통합 관리
- 비즈니스 규칙 검증 지원

### 4. SearchMetrics
- 검색 API 사용 패턴 분석
- 성능 모니터링 및 통계 수집
- 인기 검색어, 정렬 필드 사용 빈도 추적

## 사용 방법

### 1. 컨트롤러에 @ValidPageable 적용

```java
@RestController
@RequestMapping("/api/todos")
public class TodoController {
    
    @GetMapping
    @ValidPageable(sortFields = SortFieldsProvider.TODO)
    public Page<TodoResponse> getTodos(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        // Pageable이 자동으로 검증됨
        return todoService.findAll(pageable);
    }
}
```

### 2. SearchRequest DTO 구현

```java
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TodoSearchRequest extends BaseSearchRequest {
    
    private String keyword;
    private Boolean complete;
    private List<UUID> categoryIds;
    
    @Override
    public String getDefaultSort() {
        return "createdAt,desc";
    }
    
    @Override
    protected void validateBusinessRules() {
        // 도메인별 검증 로직
        if (categoryIds != null && categoryIds.size() > 50) {
            throw new IllegalArgumentException("카테고리는 최대 50개까지 선택 가능");
        }
    }
}
```

### 3. 허용된 정렬 필드 정의

```java
public final class AllowedSortFields {
    
    public static final Set<String> TODO_FIELDS = Set.of(
        "id",
        "createdAt",
        "updatedAt",
        "title",
        "complete",
        "date",
        "priorityId",
        "orderIndex",
        "owner.nickname",
        "category.title"
    );
}
```

## 검증 규칙

### 페이지 크기 제한
- 최소: 1
- 최대: 100
- 기본값: 20

### 페이지 번호 제한
- 최소: 0
- 최대: 10000
- 기본값: 0

### 정렬 필드
- 화이트리스트 기반 검증
- 패턴: `^[a-zA-Z][a-zA-Z0-9_]*(\.[a-zA-Z][a-zA-Z0-9_]*)*$`
- SQL Injection 방지

## 응답 형식

### PageResponse DTO

```java
{
    "content": [...],      // 현재 페이지 데이터
    "page": 0,            // 현재 페이지 번호
    "size": 20,           // 페이지 크기
    "totalElements": 100, // 전체 요소 수
    "totalPages": 5,      // 전체 페이지 수
    "first": true,        // 첫 페이지 여부
    "last": false,        // 마지막 페이지 여부
    "numberOfElements": 20, // 현재 페이지 요소 수
    "empty": false        // 빈 페이지 여부
}
```

## 모니터링 및 메트릭

### 검색 메트릭 수집
- 검색 요청 수
- 성공/실패 비율
- 평균 응답 시간
- 인기 페이지 크기
- 자주 사용되는 정렬 필드

### 메트릭 조회 API

```http
GET /api/admin/metrics/search
Authorization: Bearer {admin-token}

Response:
{
    "totalRequests": 10000,
    "successfulRequests": 9950,
    "failedRequests": 50,
    "averageResponseTime": 125.5,
    "popularPageSizes": {
        "20": 5000,
        "50": 3000,
        "10": 2000
    },
    "popularSortFields": {
        "createdAt": 7000,
        "title": 2000,
        "date": 1000
    }
}
```

## 성능 최적화

### 1. 느린 쿼리 감지
- 1초 이상 소요되는 쿼리 자동 로깅
- 쿼리 패턴 분석을 통한 인덱스 추천

### 2. 인덱스 추천 조회

```http
GET /api/admin/metrics/index-recommendations
Authorization: Bearer {admin-token}

Response:
{
    "recommendations": [
        {
            "entityName": "Todo",
            "fields": ["owner.id", "active", "date"],
            "executionCount": 1500,
            "averageExecutionTime": 85.3,
            "indexName": "idx_todo_owner_id_active_date"
        }
    ],
    "sqlStatements": [
        "CREATE INDEX idx_todo_owner_id_active_date ON todo (owner_id, active, date);",
        "-- Recommended: 1500 executions, avg 85.30ms"
    ]
}
```

## 에러 처리

### 검증 실패 시 응답

```json
{
    "type": "https://example.com/probs/invalid-input",
    "title": "Invalid Input",
    "status": 400,
    "detail": "Page size cannot exceed 100",
    "instance": "/api/todos?page=0&size=200",
    "errorCode": "INVALID_INPUT_VALUE",
    "timestamp": "2024-01-15T10:30:00"
}
```

### 일반적인 에러 케이스
1. 페이지 크기 초과: "Page size cannot exceed 100"
2. 음수 페이지 번호: "Page number cannot be negative"
3. 허용되지 않은 정렬 필드: "Sort field not allowed: {field}"
4. 잘못된 정렬 패턴: "Invalid sort field: {field}"

## 베스트 프랙티스

### 1. 적절한 페이지 크기 선택
- 목록 화면: 20-30
- 그리드 뷰: 50
- 엑셀 다운로드: 100 (별도 API 권장)

### 2. 정렬 필드 최소화
- 인덱스가 있는 필드만 정렬 허용
- 복합 정렬은 성능 저하 가능

### 3. 검색 조건 최적화
- 불필요한 조건 제거
- 인덱스를 활용할 수 있는 조건 우선

### 4. 캐싱 고려
- 자주 조회되는 페이지는 캐싱
- 정렬/필터가 없는 기본 목록 캐싱

## 문제 해결

### 1. "Sort field not allowed" 오류
- AllowedSortFields에 필드 추가
- 또는 SortFieldsProvider 확인

### 2. 느린 페이지 로딩
- 검색 메트릭 확인
- 인덱스 추천 확인 및 적용
- 페이지 크기 줄이기

### 3. 검증 우회 시도 감지
- 로그 모니터링
- 비정상적인 패턴 감지 시 보안팀 알림