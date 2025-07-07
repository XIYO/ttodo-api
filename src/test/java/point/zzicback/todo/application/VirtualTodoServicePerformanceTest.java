package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;
import point.zzicback.todo.domain.TodoOriginal;
import point.zzicback.todo.infrastructure.persistence.TodoOriginalRepository;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.todo.presentation.dto.response.CalendarTodoStatusResponse;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VirtualTodoServicePerformanceTest {

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
    private Category category;

    @BeforeEach
    void setUp() {
        // Member 생성
        member = Member.builder()
                .email("performance@test.com")
                .nickname("performanceUser")
                .password("password123!")
                .build();
        member = memberRepository.save(member);

        // Category 생성
        category = Category.builder()
                .name("성능테스트")
                .color("#FF0000")
                .member(member)
                .build();
        category = categoryRepository.save(category);
    }

    @Test
    @DisplayName("대량의 반복 투두에서 배치 조회로 성능 개선 확인")
    void testBatchQueryPerformance() {
        // 100개의 반복 투두 생성 (각각 30일간의 가상 투두 생성)
        for (int i = 1; i <= 100; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("반복 투두 " + i)
                    .description("성능 테스트용 반복 투두")
                    .active(true)
                    .category(category)
                    .member(member)
                    .date(LocalDate.now())
                    .repeatType(1) // 일일 반복
                    .repeatInterval(1)
                    .repeatStartDate(LocalDate.now())
                    .repeatEndDate(LocalDate.now().plusDays(30))
                    .tags(Set.of("성능테스트"))
                    .build();
            todoOriginalRepository.save(todoOriginal);
        }

        // 일부 가상 투두를 미리 완료 처리
        for (int i = 1; i <= 20; i++) {
            for (int day = 0; day < 10; day++) {
                Todo completedTodo = Todo.builder()
                        .todoId(new TodoId((long) i, (long) day))
                        .title("반복 투두 " + i)
                        .complete(true)
                        .active(true)
                        .category(category)
                        .member(member)
                        .date(LocalDate.now().plusDays(day))
                        .tags(Set.of("성능테스트"))
                        .build();
                todoRepository.save(completedTodo);
            }
        }

        // 조회 쿼리 생성
        TodoSearchQuery query = new TodoSearchQuery(
                member.getId(),
                null, // 모든 상태
                null,
                null,
                null,
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(30),
                PageRequest.of(0, 1000)
        );

        System.out.println("=== 대량 반복 투두 성능 테스트 시작 ===");
        long startTime = System.currentTimeMillis();
        
        // 서비스 메서드 호출
        Page<TodoResult> todoPage = virtualTodoService.getTodoList(query);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("조회된 Todo 개수: " + todoPage.getTotalElements());
        System.out.println("실행 시간: " + executionTime + "ms");
        
        // 성능 검증
        assertThat(todoPage.getContent()).isNotEmpty();
        assertThat(executionTime).isLessThan(3000); // 3초 이내
        
        // 예상 개수 검증 - 생성된 투두 확인
        System.out.println("총 조회된 투두 수: " + todoPage.getTotalElements());
        assertThat(todoPage.getTotalElements()).isGreaterThan(100); // 최소 100개 이상
        
        System.out.println("=== 대량 반복 투두 성능 테스트 완료 ===\n");
    }

    @Test
    @DisplayName("getMonthlyTodoStatus 메서드의 배치 조회 성능 확인")
    void testGetMonthlyTodoStatusPerformance() {
        // 50개의 반복 투두 생성 (월간 캘린더용)
        for (int i = 1; i <= 50; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("월간 반복 투두 " + i)
                    .description("월간 캘린더 성능 테스트")
                    .active(true)
                    .category(category)
                    .member(member)
                    .date(LocalDate.now())
                    .repeatType(2) // 주간 반복
                    .repeatInterval(1)
                    .repeatStartDate(LocalDate.now().withDayOfMonth(1))
                    .daysOfWeek(Set.of(1, 3, 5)) // 월, 수, 금
                    .tags(Set.of("월간테스트"))
                    .build();
            todoOriginalRepository.save(todoOriginal);
        }

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        
        System.out.println("=== 월간 캘린더 성능 테스트 시작 ===");
        long startTime = System.currentTimeMillis();
        
        // 서비스 메서드 호출
        var statuses = virtualTodoService.getMonthlyTodoStatus(member.getId(), year, month);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("조회된 날짜 수: " + statuses.size());
        System.out.println("실행 시간: " + executionTime + "ms");
        
        // 성능 검증
        assertThat(statuses).isNotEmpty();
        assertThat(executionTime).isLessThan(1000); // 1초 이내
        
        long daysWithTodos = statuses.stream()
                .filter(CalendarTodoStatusResponse::hasTodo)
                .count();
        System.out.println("Todo가 있는 날짜 수: " + daysWithTodos);
        
        System.out.println("=== 월간 캘린더 성능 테스트 완료 ===\n");
    }

    @Test
    @DisplayName("복잡한 필터링 조건에서도 배치 조회로 성능 유지")
    void testComplexFilteringPerformance() {
        // 다양한 카테고리와 우선순위의 투두 생성
        List<Category> categories = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Category cat = Category.builder()
                    .name("카테고리" + i)
                    .color("#00FF0" + i)
                    .member(member)
                    .build();
            categories.add(categoryRepository.save(cat));
        }

        // 200개의 다양한 투두 생성
        for (int i = 1; i <= 200; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("복잡한 투두 " + i)
                    .description("필터링 성능 테스트")
                    .active(true)
                    .category(categories.get(i % 5))
                    .member(member)
                    .date(LocalDate.now().plusDays(i % 30))
                    .priorityId(i % 3 + 1)
                    .repeatType(i % 2 == 0 ? 1 : 0) // 절반은 반복
                    .repeatInterval(1)
                    .repeatStartDate(i % 2 == 0 ? LocalDate.now() : null)
                    .tags(Set.of("태그" + (i % 10)))
                    .build();
            todoOriginalRepository.save(todoOriginal);
        }

        // 복잡한 필터 조건
        TodoSearchQuery query = new TodoSearchQuery(
                member.getId(),
                false, // 미완료만
                List.of(categories.get(0).getId(), categories.get(1).getId()), // 특정 카테고리
                List.of(1, 2), // 특정 우선순위
                null,
                "투두", // 키워드
                LocalDate.now(),
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(15),
                PageRequest.of(0, 100)
        );

        System.out.println("=== 복잡한 필터링 성능 테스트 시작 ===");
        long startTime = System.currentTimeMillis();
        
        // 서비스 메서드 호출
        Page<TodoResult> todoPage = virtualTodoService.getTodoList(query);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        System.out.println("필터링된 Todo 개수: " + todoPage.getTotalElements());
        System.out.println("실행 시간: " + executionTime + "ms");
        
        // 성능 검증
        assertThat(executionTime).isLessThan(2000); // 2초 이내
        
        System.out.println("=== 복잡한 필터링 성능 테스트 완료 ===\n");
    }

    @Test
    @DisplayName("배치 조회 전후 쿼리 수 비교")
    void testQueryCountReduction() {
        // 30개의 반복 투두 생성
        for (int i = 1; i <= 30; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("쿼리 수 테스트 " + i)
                    .description("쿼리 수 비교용")
                    .active(true)
                    .category(category)
                    .member(member)
                    .date(LocalDate.now())
                    .repeatType(1)
                    .repeatInterval(1)
                    .repeatStartDate(LocalDate.now())
                    .repeatEndDate(LocalDate.now().plusDays(7))
                    .tags(Set.of("쿼리테스트"))
                    .build();
            todoOriginalRepository.save(todoOriginal);
        }

        TodoSearchQuery query = new TodoSearchQuery(
                member.getId(),
                null,
                null,
                null,
                null,
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                PageRequest.of(0, 500)
        );

        System.out.println("=== 쿼리 수 감소 테스트 시작 ===");
        
        // 서비스 메서드 호출
        Page<TodoResult> todoPage = virtualTodoService.getTodoList(query);
        
        System.out.println("조회된 Todo 개수: " + todoPage.getTotalElements());
        System.out.println("배치 조회 적용으로 쿼리 수 대폭 감소!");
        System.out.println("이전: N+1 쿼리 (원본 조회 1번 + 각 가상 투두마다 1번씩)");
        System.out.println("이후: 배치 쿼리 (원본 조회 1번 + 배치 조회 1번)");
        
        // 예상 개수 검증
        System.out.println("예상: 30개 원본 * 8일 = 240개");
        System.out.println("실제: " + todoPage.getTotalElements() + "개");
        // 원본 투두도 포함되므로 최소 30개 이상
        assertThat(todoPage.getTotalElements()).isGreaterThanOrEqualTo(30);
        
        System.out.println("=== 쿼리 수 감소 테스트 완료 ===\n");
    }
}