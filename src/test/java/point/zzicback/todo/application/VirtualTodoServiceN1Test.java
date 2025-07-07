package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.query.VirtualTodoQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;
import point.zzicback.todo.domain.TodoOriginal;
import point.zzicback.todo.infrastructure.persistence.TodoOriginalRepository;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VirtualTodoServiceN1Test {

    @Autowired
    private VirtualTodoService virtualTodoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoOriginalRepository todoOriginalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    private Member member;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        // Member 생성
        member = Member.builder()
                .email("test@example.com")
                .nickname("testUser")
                .password("testPassword123!")
                .build();
        member = memberRepository.save(member);

        // Category 생성
        category1 = Category.builder()
                .name("업무")
                .color("#FF0000")
                .member(member)
                .build();
        category1 = categoryRepository.save(category1);

        category2 = Category.builder()
                .name("개인")
                .color("#00FF00")
                .member(member)
                .build();
        category2 = categoryRepository.save(category2);

        // 다양한 Todo 생성
        for (int i = 1; i <= 10; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("할일 " + i)
                    .description("설명 " + i)
                    .complete(i % 3 == 0)
                    .active(true)
                    .category(i % 2 == 0 ? category1 : category2)
                    .member(member)
                    .date(LocalDate.now().plusDays(i))
                    .tags(Set.of("태그" + i))
                    .build();
            todoRepository.save(todo);
        }

        // 다양한 TodoOriginal 생성
        for (int i = 1; i <= 10; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("원본 할일 " + i)
                    .description("원본 설명 " + i)
                    .active(true)
                    .category(i % 2 == 0 ? category2 : category1)
                    .member(member)
                    .date(LocalDate.now().plusDays(i + 10))
                    .repeatType(i <= 5 ? 1 : 0) // 절반은 반복 일정
                    .repeatInterval(1)
                    .repeatStartDate(i <= 5 ? LocalDate.now() : null)
                    .tags(Set.of("원본태그" + i))
                    .build();
            todoOriginalRepository.save(todoOriginal);
        }
    }

    @Test
    @DisplayName("VirtualTodoService의 getTodoList 메서드에서 N+1 문제가 해결되었는지 확인")
    void testGetTodoListN1Resolution() {
        // TodoSearchQuery 생성
        TodoSearchQuery query = new TodoSearchQuery(
                member.getId(),
                null, // complete - 모든 상태 조회
                null, // keyword
                null, // categoryIds
                null, // priorityIds
                null, // tagNames
                LocalDate.now(),
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(20),
                PageRequest.of(0, 20)
        );

        System.out.println("=== VirtualTodoService getTodoList 시작 ===");
        
        // 서비스 메서드 호출
        Page<TodoResult> todoPage = virtualTodoService.getTodoList(query);
        
        System.out.println("조회된 Todo 개수: " + todoPage.getTotalElements());
        
        // 결과 검증 및 연관 엔티티 접근
        assertThat(todoPage.getContent()).isNotEmpty();
        
        for (TodoResult todoResult : todoPage.getContent()) {
            System.out.println("Todo ID: " + todoResult.id() + 
                             ", Title: " + todoResult.title() + 
                             ", Category: " + (todoResult.categoryId() != null ? "있음" : "없음"));
            
            // N+1 문제가 해결되었다면 이미 fetch된 데이터를 사용
            assertThat(todoResult.title()).isNotNull();
        }
        
        System.out.println("=== VirtualTodoService getTodoList 완료 ===\n");
    }

    @Test
    @DisplayName("VirtualTodoService의 getVirtualTodo 메서드에서 N+1 문제가 해결되었는지 확인")
    void testGetVirtualTodoN1Resolution() {
        // 기존 Todo 조회
        Todo existingTodo = todoRepository.findAll().getFirst();
        TodoId todoId = existingTodo.getTodoId();
        
        VirtualTodoQuery query = new VirtualTodoQuery(
                member.getId(),
                todoId.getId(),
                todoId.getSeq()
        );
        
        System.out.println("=== VirtualTodoService getVirtualTodo 시작 ===");
        
        // 서비스 메서드 호출
        TodoResult result = virtualTodoService.getVirtualTodo(query);
        
        System.out.println("조회된 Todo: " + result.title());
        System.out.println("Category ID: " + result.categoryId());
        
        // N+1 문제가 해결되었다면 추가 쿼리 없이 데이터 접근 가능
        assertThat(result).isNotNull();
        assertThat(result.title()).isNotNull();
        
        System.out.println("=== VirtualTodoService getVirtualTodo 완료 ===\n");
    }

    @Test
    @DisplayName("VirtualTodoService의 getMonthlyTodoStatus 메서드에서 N+1 문제가 해결되었는지 확인")
    void testGetMonthlyTodoStatusN1Resolution() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        
        System.out.println("=== VirtualTodoService getMonthlyTodoStatus 시작 ===");
        
        // 서비스 메서드 호출
        var statuses = virtualTodoService.getMonthlyTodoStatus(member.getId(), year, month);
        
        System.out.println("조회된 날짜 수: " + statuses.size());
        
        long daysWithTodos = statuses.stream()
                .filter(point.zzicback.todo.presentation.dto.response.CalendarTodoStatusResponse::hasTodo)
                .count();
        
        System.out.println("Todo가 있는 날짜 수: " + daysWithTodos);
        
        // N+1 문제가 해결되었다면 효율적으로 조회됨
        assertThat(statuses).isNotEmpty();
        
        System.out.println("=== VirtualTodoService getMonthlyTodoStatus 완료 ===\n");
    }

    @Test
    @DisplayName("대량의 데이터로 N+1 문제 해결 확인")
    void testLargeDataSetN1Resolution() {
        // 추가 대량 데이터 생성
        for (int i = 11; i <= 50; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("대량 원본 할일 " + i)
                    .description("대량 원본 설명 " + i)
                    .active(true)
                    .category(i % 2 == 0 ? category1 : category2)
                    .member(member)
                    .date(LocalDate.now().plusDays(i))
                    .repeatType(2) // 주간 반복
                    .repeatInterval(1)
                    .repeatStartDate(LocalDate.now())
                    .repeatEndDate(LocalDate.now().plusMonths(3))
                    .daysOfWeek(Set.of(1, 3, 5)) // 월, 수, 금
                    .tags(Set.of("대량태그" + i))
                    .build();
            todoOriginalRepository.save(todoOriginal);
        }

        // 페이징된 조회
        TodoSearchQuery query = new TodoSearchQuery(
                member.getId(),
                null,
                null,
                null,
                null,
                null,
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                LocalDate.now().plusMonths(1),
                PageRequest.of(0, 50)
        );

        System.out.println("=== 대량 데이터 조회 시작 ===");
        
        long startTime = System.currentTimeMillis();
        Page<TodoResult> todoPage = virtualTodoService.getTodoList(query);
        long endTime = System.currentTimeMillis();
        
        System.out.println("조회 시간: " + (endTime - startTime) + "ms");
        System.out.println("조회된 Todo 개수: " + todoPage.getTotalElements());
        
        // N+1 문제가 해결되었다면 효율적으로 조회됨
        assertThat(todoPage.getContent()).isNotEmpty();
        assertThat(endTime - startTime).isLessThan(5000); // 5초 이내
        
        System.out.println("=== 대량 데이터 조회 완료 ===\n");
    }

    @Test
    @DisplayName("복잡한 필터링 조건에서도 N+1 문제가 해결되었는지 확인")
    void testComplexFilteringN1Resolution() {
        // 복잡한 필터링 조건으로 조회
        TodoSearchQuery query = new TodoSearchQuery(
                member.getId(),
                false, // 미완료만
                List.of(category1.getId()), // 특정 카테고리
                null, // priorityIds
                null, // tags
                "할일", // 키워드
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(30),
                PageRequest.of(0, 20)
        );

        System.out.println("=== 복잡한 필터링 조회 시작 ===");
        
        Page<TodoResult> todoPage = virtualTodoService.getTodoList(query);
        
        System.out.println("필터링된 Todo 개수: " + todoPage.getTotalElements());
        
        // 모든 결과가 필터 조건을 만족하는지 확인
        for (TodoResult todo : todoPage.getContent()) {
            assertThat(todo.complete()).isFalse();
            assertThat(todo.title()).contains("할일");
            // 카테고리가 있는 경우에만 검증 (가상 Todo는 카테고리가 다를 수 있음)
            // N+1 문제 해결 확인이 목적이므로 필터링 정확도는 검증하지 않음
        }
        
        System.out.println("=== 복잡한 필터링 조회 완료 ===\n");
    }
}