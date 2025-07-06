# 프로젝트 규칙

## 1. 커밋 규칙
- 깃모지(Gitmoji)를 사용하여 커밋 메시지 작성
- 커밋 메시지는 한글로 작성
- 형식: `[깃모지] 커밋 메시지`

### 주요 깃모지
- ✨ `:sparkles:` 새로운 기능 추가
- 🐛 `:bug:` 버그 수정
- ♻️ `:recycle:` 코드 리팩토링
- 🔧 `:wrench:` 설정 파일 수정
- 📝 `:memo:` 문서 수정
- 🎨 `:art:` 코드 구조/형식 개선
- ⚡️ `:zap:` 성능 개선
- 🔥 `:fire:` 코드/파일 삭제
- ✅ `:white_check_mark:` 테스트 추가/수정
- 🚀 `:rocket:` 배포 관련
- 💄 `:lipstick:` UI/스타일 파일 추가/수정
- 🏗️ `:building_construction:` 아키텍처 변경
- 🔒 `:lock:` 보안 이슈 수정
- ⬆️ `:arrow_up:` 의존성 업그레이드
- ⬇️ `:arrow_down:` 의존성 다운그레이드

### 커밋 예시
```
✨ 사용자 프로필 관리 기능 추가
🐛 로그인 시 토큰 만료 버그 수정
♻️ MemberProfile 엔티티 DB 이식성 개선
```

## 2. 코드 스타일
- 주석은 한글로 작성
- 변수명과 메서드명은 영어로 작성
- 클래스 및 메서드에 JavaDoc 주석 추가 권장
- 코드 포맷팅은 IntelliJ 기본 Java 스타일 사용

## 3. 아키텍처 및 패키지 구조
### 패키지 구조
```
point.zzicback
├── [도메인명]
│   ├── domain          # 도메인 엔티티 및 비즈니스 로직
│   ├── application     # 서비스 계층 (비즈니스 로직)
│   ├── infrastructure  # 인프라 계층 (Repository, 외부 연동)
│   ├── presentation    # 프레젠테이션 계층 (Controller, DTO)
│   └── config         # 설정 클래스
└── common             # 공통 유틸리티 및 설정
```

### 레이어별 책임
- **Domain**: 순수 비즈니스 로직, 엔티티, 도메인 서비스
- **Application**: 유스케이스 구현, 트랜잭션 관리, DTO 변환
- **Infrastructure**: 데이터베이스 접근, 외부 API 호출, 파일 시스템 접근
- **Presentation**: HTTP 요청/응답 처리, 입력 검증, API 문서화

## 4. 에러 처리 규칙

### 4.1 ProblemDetail 사용
- Spring Framework 6.0의 `ProblemDetail` 클래스를 사용하여 일관된 에러 응답 구조 유지
- RFC 7807 (Problem Details for HTTP APIs) 표준 준수

### 4.2 에러 응답 구조
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getHttpStatus(), 
            ex.getMessage()
        );
        problemDetail.setTitle(ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        
        return ResponseEntity.status(ex.getHttpStatus()).body(problemDetail);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "입력값 검증 실패"
        );
        problemDetail.setTitle("VALIDATION_ERROR");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        problemDetail.setProperty("errors", errors);
        
        return ResponseEntity.badRequest().body(problemDetail);
    }
}
```

### 4.3 커스텀 예외 클래스
```java
public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
```

### 4.4 에러 코드 관리
```java
public enum ErrorCode {
    // 인증 관련
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH_002", "접근 권한이 없습니다", HttpStatus.FORBIDDEN),
    
    // 리소스 관련
    NOT_FOUND("RESOURCE_001", "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    CONFLICT("RESOURCE_002", "리소스 충돌이 발생했습니다", HttpStatus.CONFLICT),
    
    // 비즈니스 로직
    INVALID_REQUEST("BIZ_001", "잘못된 요청입니다", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
}
```

## 5. 테스트 규칙

### 5.1 테스트 구조
- 단위 테스트: 도메인 로직, 서비스 로직 테스트
- 통합 테스트: 컨트롤러, Repository 테스트
- E2E 테스트: 전체 시나리오 테스트

### 5.2 통합 테스트 규칙
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class IntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    // 실제 HTTP 요청을 통한 테스트
}
```

### 5.3 테스트 명명 규칙
- 메서드명: `테스트대상_시나리오_기대결과`
- DisplayName: 한글로 명확하게 작성

### 5.4 에러 응답 검증 규칙
- 모든 실패 케이스는 ProblemDetail 형식으로 응답하는지 검증
- 에러 응답 구조 확인 필수

```java
@Test
@DisplayName("존재하지 않는 리소스 조회 시 404 에러")
void getResource_NotFound_Returns404WithProblemDetail() {
    // When
    ResponseEntity<ProblemDetail> response = restTemplate.exchange(
        "/api/resources/999",
        HttpMethod.GET,
        null,
        ProblemDetail.class
    );
    
    // Then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    
    ProblemDetail problemDetail = response.getBody();
    assertNotNull(problemDetail);
    assertEquals(404, problemDetail.getStatus());
    assertEquals("RESOURCE_001", problemDetail.getProperties().get("errorCode"));
    assertNotNull(problemDetail.getDetail());
    assertNotNull(problemDetail.getTitle());
    assertNotNull(problemDetail.getInstance());
}

@Test
@DisplayName("유효하지 않은 입력값으로 요청 시 400 에러")
void createResource_InvalidInput_Returns400WithProblemDetail() {
    // Given
    Map<String, Object> invalidRequest = Map.of("title", ""); // 빈 제목
    
    // When
    ResponseEntity<ProblemDetail> response = restTemplate.exchange(
        "/api/resources",
        HttpMethod.POST,
        new HttpEntity<>(invalidRequest),
        ProblemDetail.class
    );
    
    // Then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    
    ProblemDetail problemDetail = response.getBody();
    assertNotNull(problemDetail);
    assertEquals(400, problemDetail.getStatus());
    assertEquals("VALIDATION_ERROR", problemDetail.getTitle());
    
    // 검증 에러 상세 정보 확인
    Map<String, String> errors = (Map<String, String>) problemDetail.getProperties().get("errors");
    assertNotNull(errors);
    assertTrue(errors.containsKey("title"));
}
```

### 5.5 테스트용 ProblemDetail 검증 유틸리티
```java
public class ProblemDetailAssert {
    
    public static void assertProblemDetail(ResponseEntity<ProblemDetail> response, 
                                         HttpStatus expectedStatus,
                                         String expectedErrorCode) {
        assertEquals(expectedStatus, response.getStatusCode());
        
        ProblemDetail problemDetail = response.getBody();
        assertNotNull(problemDetail, "ProblemDetail 응답이 null입니다");
        assertEquals(expectedStatus.value(), problemDetail.getStatus());
        assertEquals(expectedErrorCode, problemDetail.getProperties().get("errorCode"));
        assertNotNull(problemDetail.getDetail(), "에러 상세 메시지가 없습니다");
        assertNotNull(problemDetail.getTitle(), "에러 제목이 없습니다");
        assertNotNull(problemDetail.getProperties().get("timestamp"), "타임스탬프가 없습니다");
    }
    
    public static void assertValidationErrors(ProblemDetail problemDetail, 
                                            String... expectedFields) {
        assertEquals("VALIDATION_ERROR", problemDetail.getTitle());
        
        Map<String, String> errors = (Map<String, String>) problemDetail.getProperties().get("errors");
        assertNotNull(errors, "검증 에러 상세 정보가 없습니다");
        
        for (String field : expectedFields) {
            assertTrue(errors.containsKey(field), 
                      field + " 필드에 대한 검증 에러가 없습니다");
        }
    }
}
```

## 6. API 설계 규칙

### 6.1 RESTful API 설계
- 명사형 리소스 이름 사용
- HTTP 메서드 의미에 맞게 사용
  - GET: 조회
  - POST: 생성
  - PUT: 전체 수정
  - PATCH: 부분 수정
  - DELETE: 삭제

### 6.2 응답 코드
- 200 OK: 성공적인 조회/수정
- 201 Created: 성공적인 생성
- 204 No Content: 성공적인 삭제/수정 (응답 본문 없음)
- 400 Bad Request: 잘못된 요청
- 401 Unauthorized: 인증 필요
- 403 Forbidden: 권한 없음
- 404 Not Found: 리소스 없음
- 409 Conflict: 충돌 발생

### 6.3 페이징 처리
- Spring Data의 `Pageable` 사용
- 쿼리 파라미터: `page`, `size`, `sort`

## 7. 보안 규칙

### 7.1 인증/인가
- JWT 토큰 기반 인증
- Spring Security 사용
- `@AuthenticationPrincipal` 사용하여 현재 사용자 정보 주입

### 7.2 데이터 보안
- 민감한 정보는 로그에 남기지 않음
- 비밀번호는 BCrypt로 암호화
- SQL Injection 방지를 위해 파라미터 바인딩 사용

## 8. 데이터베이스 규칙

### 8.1 엔티티 설계
- 모든 엔티티는 `id` 필드 필수
- 생성일시(`createdAt`), 수정일시(`updatedAt`) 필드 추가
- Soft Delete 사용 시 `active` 또는 `deletedAt` 필드 사용

### 8.2 네이밍 규칙
- 테이블명: 소문자, 언더스코어 구분 (snake_case)
- 컬럼명: 소문자, 언더스코어 구분 (snake_case)
- 인덱스명: `idx_테이블명_컬럼명`

### 8.3 JPA 사용 규칙
- Lazy Loading 기본 사용
- N+1 문제 주의 (fetch join 또는 @EntityGraph 사용)
- 대량 데이터 처리 시 JPQL 또는 네이티브 쿼리 사용

## 9. 성능 최적화

### 9.1 캐싱
- Spring Cache 추상화 사용
- 자주 조회되고 변경이 적은 데이터 캐싱

### 9.2 비동기 처리
- 시간이 오래 걸리는 작업은 `@Async` 사용
- 이벤트 기반 아키텍처 고려

## 10. 문서화

### 10.1 API 문서
- SpringDoc OpenAPI (Swagger) 사용
- 모든 API 엔드포인트에 설명 추가

### 10.2 코드 문서
- 복잡한 비즈니스 로직은 JavaDoc으로 설명
- README.md 파일 최신 상태 유지

## 11. DDD 매퍼 규칙

### 11.1 레이어별 DTO 및 매퍼 책임
DDD의 의존성 규칙: `Presentation → Application → Domain` (외부 → 내부)

#### DTO 구조
```
presentation/dto/
├── request/     # API 요청 DTO
└── response/    # API 응답 DTO

application/dto/
├── command/     # 명령 실행용 DTO
├── query/       # 조회용 DTO  
└── result/      # 서비스 결과 DTO
```

#### 매퍼 위치 및 변환 방향
1. **Application Layer Mapper**
   - 위치: `application/mapper/`
   - 역할: `Entity → Result` (내부에서 내부로)
   - 예시: `CategoryApplicationMapper`

2. **Presentation Layer Mapper**
   - 위치: `presentation/mapper/`
   - 역할: `Result → Response` (내부를 외부로)
   - 예시: `CategoryPresentationMapper`

### 11.2 매퍼 규칙
- **내부 레이어는 외부 레이어를 모름**: Application Layer는 Request/Response를 모름
- **외부 레이어가 내부 객체를 알고 변환**: Presentation Layer가 Result를 Response로 변환
- **단방향 의존성**: 매퍼는 상위 레이어의 객체를 참조하지 않음

### 11.3 구현 예시
```java
// Application Layer - 내부만 알고 있음
@Service
public class CategoryService {
    public CategoryResult getCategory(Long id) {
        Category entity = repository.findById(id);
        return toCategoryResult(entity);  // Entity → Result
    }
}

// Presentation Layer - 내부를 알고 외부로 변환
@RestController  
public class CategoryController {
    private final CategoryPresentationMapper mapper;
    
    public CategoryResponse getCategory(Long id) {
        CategoryResult result = service.getCategory(id);
        return mapper.toResponse(result);  // Result → Response
    }
}
```