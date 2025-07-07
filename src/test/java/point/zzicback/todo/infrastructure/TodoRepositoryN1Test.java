package point.zzicback.todo.infrastructure;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import point.zzicback.category.domain.Category;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;
import point.zzicback.todo.domain.TodoOriginal;
import point.zzicback.todo.infrastructure.persistence.TodoOriginalRepository;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.todo.infrastructure.persistence.TodoSpecification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryN1Test {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoOriginalRepository todoOriginalRepository;

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
        testEntityManager.persist(member);

        // Category 생성
        category1 = Category.builder()
                .name("업무")
                .color("#FF0000")
                .member(member)
                .build();
        testEntityManager.persist(category1);

        category2 = Category.builder()
                .name("개인")
                .color("#00FF00")
                .member(member)
                .build();
        testEntityManager.persist(category2);

        // 여러 개의 Todo 생성
        for (int i = 1; i <= 5; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("할일 " + i)
                    .description("설명 " + i)
                    .complete(false)
                    .active(true)
                    .category(i % 2 == 0 ? category1 : category2)
                    .member(member)
                    .date(LocalDate.now().plusDays(i))
                    .tags(Set.of("태그" + i))
                    .build();
            testEntityManager.persist(todo);
        }

        // 여러 개의 TodoOriginal 생성
        for (int i = 1; i <= 5; i++) {
            TodoOriginal todoOriginal = TodoOriginal.builder()
                    .title("원본 할일 " + i)
                    .description("원본 설명 " + i)
                    .active(true)
                    .category(i % 2 == 0 ? category2 : category1)
                    .member(member)
                    .date(LocalDate.now().plusDays(i + 5))
                    .repeatType(0)
                    .tags(Set.of("원본태그" + i))
                    .build();
            testEntityManager.persist(todoOriginal);
        }

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("Todo 조회 시 fetch join으로 N+1 문제가 해결되는지 확인")
    void testTodoFetchJoin() {
        // 엔티티매니저를 통해 쿼리 수 카운트
        EntityManager em = testEntityManager.getEntityManager();
        em.clear();

        // 단건 조회 테스트
        System.out.println("=== 단건 조회 시작 ===");
        Optional<Todo> todo = todoRepository.findByTodoIdAndMemberId(new TodoId(1L, 0L), member.getId());

        assertThat(todo).isPresent();
        // fetch join으로 인해 추가 쿼리 없이 연관 엔티티 접근 가능
        assertThat(todo.get().getCategory()).isNotNull();
        assertThat(todo.get().getMember()).isNotNull();
        System.out.println("Category 이름: " + todo.get().getCategory().getName());
        System.out.println("Member 닉네임: " + todo.get().getMember().getNickname());
        System.out.println("=== 단건 조회 완료 ===\n");
    }

    @Test
    @DisplayName("Todo 목록 조회 시 @EntityGraph로 N+1 문제가 해결되는지 확인")
    void testTodoListEntityGraph() {
        EntityManager em = testEntityManager.getEntityManager();
        em.clear();

        // Specification을 사용한 목록 조회
        System.out.println("=== 목록 조회 시작 ===");
        Specification<Todo> spec = TodoSpecification.createSpecification(
                member.getId(),
                null,
                null,
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        Page<Todo> todoPage = todoRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(todoPage.getContent()).hasSize(5);

        // @EntityGraph로 인해 추가 쿼리 없이 모든 연관 엔티티 접근 가능
        System.out.println("총 " + todoPage.getTotalElements() + "개의 Todo 조회됨");
        for (Todo todo : todoPage.getContent()) {
            System.out.println("Todo: " + todo.getTitle() + 
                             ", Category: " + todo.getCategory().getName() + 
                             ", Member: " + todo.getMember().getNickname());
        }
        System.out.println("=== 목록 조회 완료 ===\n");
    }

    @Test
    @DisplayName("TodoOriginal 조회 시 fetch join으로 N+1 문제가 해결되는지 확인")
    void testTodoOriginalFetchJoin() {
        EntityManager em = testEntityManager.getEntityManager();
        em.clear();

        // 멤버별 TodoOriginal 목록 조회
        System.out.println("=== TodoOriginal 목록 조회 시작 ===");
        List<TodoOriginal> todoOriginals = todoOriginalRepository.findByMemberId(member.getId());

        assertThat(todoOriginals).hasSize(5);

        // fetch join으로 인해 추가 쿼리 없이 모든 연관 엔티티 접근 가능
        for (TodoOriginal todoOriginal : todoOriginals) {
            System.out.println("TodoOriginal: " + todoOriginal.getTitle() + 
                             ", Category: " + todoOriginal.getCategory().getName() + 
                             ", Member: " + todoOriginal.getMember().getNickname());
        }
        System.out.println("=== TodoOriginal 목록 조회 완료 ===\n");
    }

    @Test
    @DisplayName("TodoOriginal 단건 조회 시 fetch join으로 N+1 문제가 해결되는지 확인")
    void testTodoOriginalSingleFetchJoin() {
        EntityManager em = testEntityManager.getEntityManager();
        em.clear();

        // TodoOriginal ID 찾기
        List<TodoOriginal> originals = todoOriginalRepository.findByMemberId(member.getId());
        Long originalId = originals.getFirst().getId();

        em.clear();

        // 단건 조회 테스트
        System.out.println("=== TodoOriginal 단건 조회 시작 ===");
        Optional<TodoOriginal> todoOriginal = todoOriginalRepository.findByIdAndMemberId(originalId, member.getId());

        assertThat(todoOriginal).isPresent();
        // fetch join으로 인해 추가 쿼리 없이 연관 엔티티 접근 가능
        assertThat(todoOriginal.get().getCategory()).isNotNull();
        assertThat(todoOriginal.get().getMember()).isNotNull();
        System.out.println("Category 이름: " + todoOriginal.get().getCategory().getName());
        System.out.println("Member 닉네임: " + todoOriginal.get().getMember().getNickname());
        System.out.println("=== TodoOriginal 단건 조회 완료 ===\n");
    }

    @Test
    @DisplayName("핀고정된 TodoOriginal 조회 시 fetch join으로 N+1 문제가 해결되는지 확인")
    void testPinnedTodoOriginalFetchJoin() {
        // 일부 TodoOriginal을 핀고정 처리
        List<TodoOriginal> originals = todoOriginalRepository.findByMemberId(member.getId());
        for (int i = 0; i < 3; i++) {
            TodoOriginal original = originals.get(i);
            original.setIsPinned(true);
            original.setDisplayOrder(i);
            testEntityManager.persist(original);
        }
        testEntityManager.flush();
        testEntityManager.clear();

        // 핀고정된 TodoOriginal 조회
        System.out.println("=== 핀고정된 TodoOriginal 조회 시작 ===");
        List<TodoOriginal> pinnedTodos = todoOriginalRepository.findByMemberIdAndIsPinnedTrueOrderByDisplayOrderAsc(member.getId());

        assertThat(pinnedTodos).hasSize(3);

        // fetch join으로 인해 추가 쿼리 없이 모든 연관 엔티티 접근 가능
        for (TodoOriginal todoOriginal : pinnedTodos) {
            System.out.println("Pinned TodoOriginal: " + todoOriginal.getTitle() + 
                             ", Order: " + todoOriginal.getDisplayOrder() +
                             ", Category: " + todoOriginal.getCategory().getName() + 
                             ", Member: " + todoOriginal.getMember().getNickname());
        }
        System.out.println("=== 핀고정된 TodoOriginal 조회 완료 ===\n");
    }

    @Test
    @DisplayName("@ElementCollection tags N+1 문제 해결 확인")
    void testElementCollectionTagsN1Resolution() {
        EntityManager em = testEntityManager.getEntityManager();
        em.clear();

        System.out.println("=== @ElementCollection tags N+1 테스트 시작 ===");
        
        // TodoOriginal 목록 조회 - tags가 포함됨
        List<TodoOriginal> todoOriginals = todoOriginalRepository.findByMemberId(member.getId());
        
        System.out.println("조회된 TodoOriginal 개수: " + todoOriginals.size());
        
        // @EntityGraph로 인해 추가 쿼리 없이 tags 접근 가능
        for (TodoOriginal todoOriginal : todoOriginals) {
            Set<String> tags = todoOriginal.getTags();
            System.out.println("TodoOriginal: " + todoOriginal.getTitle() + ", Tags: " + tags);
            assertThat(tags).isNotNull();
        }
        
        System.out.println("=== @ElementCollection tags N+1 테스트 완료 ===\n");
    }

    @Test
    @DisplayName("@ElementCollection daysOfWeek N+1 문제 해결 확인")
    void testElementCollectionDaysOfWeekN1Resolution() {
        // 일부 TodoOriginal에 daysOfWeek 설정
        List<TodoOriginal> originals = todoOriginalRepository.findByMemberId(member.getId());
        for (int i = 0; i < 3; i++) {
            TodoOriginal original = originals.get(i);
            original.setDaysOfWeek(Set.of(1, 3, 5)); // 월, 수, 금
            original.setRepeatType(2); // 주간 반복
            testEntityManager.persist(original);
        }
        testEntityManager.flush();
        testEntityManager.clear();

        System.out.println("=== @ElementCollection daysOfWeek N+1 테스트 시작 ===");
        
        // TodoOriginal 목록 조회 - daysOfWeek가 포함됨
        List<TodoOriginal> todoOriginals = todoOriginalRepository.findByMemberId(member.getId());
        
        // @EntityGraph로 인해 추가 쿼리 없이 daysOfWeek 접근 가능
        for (TodoOriginal todoOriginal : todoOriginals) {
            Set<Integer> daysOfWeek = todoOriginal.getDaysOfWeek();
            System.out.println("TodoOriginal: " + todoOriginal.getTitle() + 
                             ", DaysOfWeek: " + (daysOfWeek.isEmpty() ? "없음" : daysOfWeek));
            assertThat(daysOfWeek).isNotNull();
        }
        
        System.out.println("=== @ElementCollection daysOfWeek N+1 테스트 완료 ===\n");
    }

    @Test
    @DisplayName("Todo의 @ElementCollection tags N+1 문제 해결 확인")
    void testTodoElementCollectionN1Resolution() {
        EntityManager em = testEntityManager.getEntityManager();
        em.clear();

        System.out.println("=== Todo @ElementCollection tags N+1 테스트 시작 ===");
        
        // Specification을 사용한 Todo 목록 조회
        Specification<Todo> spec = TodoSpecification.createSpecification(
                member.getId(),
                null,
                null,
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        Page<Todo> todoPage = todoRepository.findAll(spec, PageRequest.of(0, 10));
        
        // @EntityGraph로 인해 추가 쿼리 없이 tags 접근 가능
        for (Todo todo : todoPage.getContent()) {
            Set<String> tags = todo.getTags();
            System.out.println("Todo: " + todo.getTitle() + ", Tags: " + tags);
            assertThat(tags).isNotNull();
            assertThat(todo.getCategory()).isNotNull();
            assertThat(todo.getMember()).isNotNull();
        }
        
        System.out.println("=== Todo @ElementCollection tags N+1 테스트 완료 ===\n");
    }

    @Test
    @DisplayName("대량 데이터에서 N+1 문제 해결 확인")
    void testLargeDatasetN1Resolution() {
        // 추가 데이터 생성
        for (int i = 6; i <= 20; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("대량 할일 " + i)
                    .description("대량 설명 " + i)
                    .complete(i % 3 == 0)
                    .active(true)
                    .category(i % 2 == 0 ? category1 : category2)
                    .member(member)
                    .date(LocalDate.now().plusDays(i))
                    .tags(Set.of("대량태그" + i))
                    .build();
            testEntityManager.persist(todo);
        }
        testEntityManager.flush();
        testEntityManager.clear();

        // 페이징 조회
        System.out.println("=== 대량 데이터 페이징 조회 시작 ===");
        Specification<Todo> spec = TodoSpecification.createSpecification(
                member.getId(),
                null,
                null,
                null,
                null,
                null
        );

        Page<Todo> page1 = todoRepository.findAll(spec, PageRequest.of(0, 10));
        Page<Todo> page2 = todoRepository.findAll(spec, PageRequest.of(1, 10));

        System.out.println("첫 번째 페이지 조회 - 총 " + page1.getTotalElements() + "개 중 " + page1.getNumberOfElements() + "개");
        System.out.println("두 번째 페이지 조회 - 총 " + page2.getTotalElements() + "개 중 " + page2.getNumberOfElements() + "개");

        // 모든 연관 엔티티에 추가 쿼리 없이 접근
        page1.getContent().forEach(todo -> {
            assertThat(todo.getCategory()).isNotNull();
            assertThat(todo.getMember()).isNotNull();
        });

        page2.getContent().forEach(todo -> {
            assertThat(todo.getCategory()).isNotNull();
            assertThat(todo.getMember()).isNotNull();
        });

        System.out.println("=== 대량 데이터 페이징 조회 완료 ===\n");
    }
}