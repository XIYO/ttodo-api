package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.query.TodoSearchQuery;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class,
    RepeatTodoService.class
})
class TodoServiceSearchTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);

        testTodo = Todo.builder()
                .title("테스트 할일")
                .description("테스트 설명")
                .statusId(0)
                .dueDate(LocalDate.now().plusDays(1))
                .tags(Set.of("학습"))
                .member(testMember)
                .build();
        todoRepository.save(testTodo);
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - status로 필터링")
    void getTodoListByStatus_Success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0),
                null, null, null, null, null, null, null,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst())
                .satisfies(todo -> {
                    assertThat(todo.title()).isEqualTo("테스트 할일");
                    assertThat(todo.description()).isEqualTo("테스트 설명");
                    assertThat(todo.statusId()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - 태그로 필터링")
    void getTodoListByTag_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null,
                List.of("학습"),
                null, null, null, null,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("TodoSearchQuery - 키워드 검색 테스트")
    void getTodoListByKeyword_Success() {
        Todo todo1 = Todo.builder()
                .title("영어 공부하기")
                .description("토익 문제집 풀기")
                .statusId(0)
                .member(testMember)
                .build();
        Todo todo2 = Todo.builder()
                .title("수학 공부하기")
                .description("미적분 연습")
                .statusId(0)
                .member(testMember)
                .build();
        todoRepository.saveAll(List.of(todo1, todo2));

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null,
                "영어",
                null, null, null,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("영어 공부하기");
    }

    @Test
    @DisplayName("TodoSearchQuery - 날짜 범위 검색 테스트")
    void getTodoListByDateRange_Success() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                startDate,
                endDate,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("TodoSearchQuery - 복합 조건 검색 테스트")
    void getTodoListByMultipleFilters_Success() {
        Category category = Category.builder()
                .name("개발")
                .member(testMember)
                .build();
        categoryRepository.save(category);

        Todo complexTodo = Todo.builder()
                .title("복합 조건 테스트")
                .description("여러 조건으로 검색")
                .statusId(0)
                .priorityId(1)
                .category(category)
                .tags(Set.of("테스트", "복합"))
                .member(testMember)
                .build();
        todoRepository.save(complexTodo);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0),
                List.of(category.getId()),
                List.of(1),
                List.of("테스트"),
                "복합",
                null, null, null,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("복합 조건 테스트");
    }

    @Test
    @DisplayName("TodoSearchQuery - 존재하지 않는 memberId로 검색")
    void getTodoListWithNonExistentMemberId_Success() {
        UUID nonExistentMemberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                nonExistentMemberId,
                null, null, null, null, null, null, null, null,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("TodoSearchQuery - 잘못된 날짜 범위 검색")
    void getTodoListWithInvalidDateRange_Success() {
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(1);
        
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                startDate,
                endDate,
                pageable);

        Page<TodoResult> result = todoService.getTodoList(query);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("여러 카테고리 ID로 태그 목록 조회")
    void getTagsByMultipleCategories() {
        Category category1 = categoryRepository.save(Category.builder()
                .name("업무")
                .member(testMember)
                .build());
        Category category2 = categoryRepository.save(Category.builder()
                .name("개인")
                .member(testMember)
                .build());

        Todo todo1 = Todo.builder()
                .title("첫 번째")
                .description("첫 번째 설명")
                .statusId(0)
                .category(category1)
                .dueDate(LocalDate.now().plusDays(1))
                .tags(Set.of("tagA"))
                .member(testMember)
                .build();
        Todo todo2 = Todo.builder()
                .title("두 번째")
                .description("두 번째 설명")
                .statusId(0)
                .category(category2)
                .dueDate(LocalDate.now().plusDays(2))
                .tags(Set.of("tagB"))
                .member(testMember)
                .build();
        todoRepository.save(todo1);
        todoRepository.save(todo2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("tag"));
        Page<String> result = todoService.getTags(
                testMember.getId(),
                List.of(category1.getId(), category1.getId(), category2.getId()),
                pageable);

        assertThat(result.getContent()).containsExactlyInAnyOrder("tagA", "tagB");
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - 중복 statusIds 허용")
    void getTodoListByDuplicateStatusIds_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                Arrays.asList(0, 0),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("TodoSearchQuery - 숨김 상태 필터 테스트")
    void getTodoListWithHideStatus_Success() {
        // given
        Todo completedTodo = Todo.builder()
                .title("완료된 할일")
                .description("완료된 할일 설명")
                .statusId(1)
                .member(testMember)
                .build();
        todoRepository.save(completedTodo);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                List.of(1), // 완료된 상태 숨김
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
        assertThat(result.getContent().getFirst().statusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("TodoSearchQuery - 빈 검색 조건으로 전체 조회")
    void getTodoListWithEmptyQuery_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("TodoSearchQuery - 매칭되지 않는 조건으로 검색")
    void getTodoListWithNoMatch_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(999), // 존재하지 않는 상태 ID
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).isEmpty();
    }
}
