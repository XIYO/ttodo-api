package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Collections;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.dto.command.*;
import point.zzicback.todo.application.dto.query.*;
import point.zzicback.todo.application.dto.result.TodoResult;
import point.zzicback.todo.application.dto.result.TodoStatistics;
import point.zzicback.todo.domain.*;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    TodoService.class,
    MemberService.class
})
class TodoServiceTestNew {

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
        // 테스트용 회원 생성
        CreateMemberCommand memberCommand = new CreateMemberCommand("test@example.com", "password", "nickname", null);
        testMember = memberService.createMember(memberCommand);

        // 테스트용 Todo 생성
        testTodo = Todo.builder()
                .title("테스트 할일")
                .description("테스트 설명")
                .statusId(0)
                .dueDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .tags(Set.of("학습"))
                .member(testMember)
                .build();
        todoRepository.save(testTodo);
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - status로 필터링")
    void getTodoListByStatus_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                List.of(0),
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
        assertThat(result.getContent().getFirst())
                .satisfies(todo -> {
                    assertThat(todo.title()).isEqualTo("테스트 할일");
                    assertThat(todo.description()).isEqualTo("테스트 설명");
                    assertThat(todo.statusId()).isEqualTo(0);
                });
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
    @DisplayName("Todo 목록 조회 성공 - 태그로 필터링")
    void getTodoListByTag_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                List.of("학습"),
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
    @DisplayName("Todo 목록 조회 성공 - 전체 조회")
    void getTodoListAll_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
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
        assertThat(result.getContent().getFirst())
                .satisfies(todo -> {
                    assertThat(todo.title()).isEqualTo("테스트 할일");
                    assertThat(todo.description()).isEqualTo("테스트 설명");
                    assertThat(todo.statusId()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("Todo 생성 성공")
    void createTodo_Success() {
        // given
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "새로운 할일",
                "새로운 설명",
                null, null, null, null, null, null
        );

        // when
        todoService.createTodo(command);

        // then
        Page<Todo> todos = todoRepository.findByMemberId(
                testMember.getId(), null, null, null,
                PageRequest.of(0, 10));
        assertThat(todos.getContent())
                .filteredOn(todo -> todo.getTitle().equals("새로운 할일"))
                .hasSize(1)
                .first()
                .satisfies(todo -> {
                    assertThat(todo.getDescription()).isEqualTo("새로운 설명");
                    assertThat(todo.getStatusId()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("Todo 수정 성공")
    void updateTodo_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "수정된 할일",
                "수정된 설명",
                1,
                null, null, null, null, null, null
        );

        // when
        todoService.updateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("수정된 할일");
        assertThat(updatedTodo.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 Todo 수정 시 예외 발생")
    void updateTodo_NotFound() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                999L,
                "수정된 할일",
                "수정된 설명",
                1,
                null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> todoService.updateTodo(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Todo 삭제 성공")
    void deleteTodo_Success() {
        // when
        todoService.deleteTodo(TodoQuery.of(testMember.getId(), testTodo.getId()));

        // then
        assertThat(todoRepository.findById(testTodo.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 Todo 삭제 시 예외 발생")
    void deleteTodo_NotFound() {
        // given
        TodoQuery query = TodoQuery.of(testMember.getId(), 999L);

        // when & then
        assertThatThrownBy(() -> todoService.deleteTodo(query))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Todo 조회 성공")
    void getTodo_Success() {
        // given
        TodoQuery query = TodoQuery.of(testMember.getId(), testTodo.getId());

        // when
        TodoResult result = todoService.getTodo(query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("테스트 할일");
        assertThat(result.description()).isEqualTo("테스트 설명");
        assertThat(result.statusId()).isEqualTo(0);
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 - 날짜 범위 필터링")
    void getTodoListByDateRange() {
        // given
        Todo extraTodo = Todo.builder()
                .title("추가 할일")
                .description("다른 설명")
                .statusId(0)
                .dueDate(LocalDate.now().plus(7, ChronoUnit.DAYS))
                .member(testMember)
                .build();
        todoRepository.save(extraTodo);

        Pageable pageable = PageRequest.of(0, 10);
        LocalDate start = LocalDate.now().plus(1, ChronoUnit.DAYS);
        LocalDate end = LocalDate.now().plus(3, ChronoUnit.DAYS);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                start,
                end,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent())
                .hasSize(1);

        // adjust range to include first todo
        query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDate.now().minus(1, ChronoUnit.DAYS),
                LocalDate.now().plus(1, ChronoUnit.DAYS),
                pageable);
        result = todoService.getTodoList(query);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("존재하지 않는 Todo 조회 시 예외 발생")
    void getTodo_NotFound() {
        // given
        TodoQuery query = TodoQuery.of(testMember.getId(), 999L);

        // when & then
        assertThatThrownBy(() -> todoService.getTodo(query))
                .isInstanceOf(EntityNotFoundException.class);
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
                .dueDate(LocalDate.now().plus(1, ChronoUnit.DAYS))
                .tags(Set.of("tagA"))
                .member(testMember)
                .build();
        Todo todo2 = Todo.builder()
                .title("두 번째")
                .description("두 번째 설명")
                .statusId(0)
                .category(category2)
                .dueDate(LocalDate.now().plus(2, ChronoUnit.DAYS))
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
    @DisplayName("TodoSearchQuery - 키워드 검색 테스트")
    void getTodoListByKeyword_Success() {
        // given
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
                null,
                null,
                null,
                null,
                "영어",
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("영어 공부하기");
    }

    @Test
    @DisplayName("TodoSearchQuery - 날짜 범위 검색 테스트")
    void getTodoListByDateRange_Success() {
        // given
        LocalDate startDate = LocalDate.now().minus(1, ChronoUnit.DAYS);
        LocalDate endDate = LocalDate.now().plus(1, ChronoUnit.DAYS);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                startDate,
                endDate,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("TodoSearchQuery - 태그 검색 테스트")
    void getTodoListByTags_Success() {
        // given
        Todo todoWithTags = Todo.builder()
                .title("태그 테스트")
                .description("태그 검색 테스트")
                .statusId(0)
                .tags(Set.of("학습", "개발"))
                .member(testMember)
                .build();
        todoRepository.save(todoWithTags);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                List.of("학습"),
                null,
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(2); // 기존 testTodo + 새로 생성한 todoWithTags
        assertThat(result.getContent())
                .extracting(TodoResult::title)
                .containsExactlyInAnyOrder("테스트 할일", "태그 테스트");
    }

    @Test
    @DisplayName("TodoSearchQuery - 복합 조건 검색 테스트")
    void getTodoListByMultipleFilters_Success() {
        // given
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
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("복합 조건 테스트");
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

    @Test
    @DisplayName("TodoSearchQuery - 여러 우선순위로 필터링")
    void getTodoListByMultiplePriorities_Success() {
        // given
        Todo highPriorityTodo = Todo.builder()
                .title("높은 우선순위")
                .description("중요한 할일")
                .statusId(0)
                .priorityId(2)
                .member(testMember)
                .build();
        Todo lowPriorityTodo = Todo.builder()
                .title("낮은 우선순위")
                .description("나중에 할 일")
                .statusId(0)
                .priorityId(0)
                .member(testMember)
                .build();
        todoRepository.saveAll(List.of(highPriorityTodo, lowPriorityTodo));

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                List.of(0, 2), // 낮은 우선순위와 높은 우선순위
                null,
                null,
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TodoResult::title)
                .containsExactlyInAnyOrder("높은 우선순위", "낮은 우선순위");
    }

    @Test
    @DisplayName("TodoSearchQuery - 제목과 설명에서 키워드 검색")
    void getTodoListByKeywordInTitleAndDescription_Success() {
        // given
        Todo todoWithKeywordInTitle = Todo.builder()
                .title("중요한 회의")
                .description("일반적인 회의")
                .statusId(0)
                .member(testMember)
                .build();
        Todo todoWithKeywordInDescription = Todo.builder()
                .title("일반적인 업무")
                .description("중요한 프로젝트 관련")
                .statusId(0)
                .member(testMember)
                .build();
        todoRepository.saveAll(List.of(todoWithKeywordInTitle, todoWithKeywordInDescription));

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                "중요한",
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TodoResult::title)
                .containsExactlyInAnyOrder("중요한 회의", "일반적인 업무");
    }

    @Test
    @DisplayName("TodoSearchQuery - 페이징 테스트")
    void getTodoListWithPaging_Success() {
        // given
        List<Todo> todos = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Todo todo = Todo.builder()
                    .title("할일 " + i)
                    .description("설명 " + i)
                    .statusId(0)
                    .member(testMember)
                    .build();
            todos.add(todo);
        }
        todoRepository.saveAll(todos);

        // 첫 번째 페이지 (크기: 5)
        Pageable firstPage = PageRequest.of(0, 5, Sort.by("id").ascending());
        TodoSearchQuery firstQuery = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                firstPage);

        // when
        Page<TodoResult> firstResult = todoService.getTodoList(firstQuery);

        // then
        assertThat(firstResult.getContent()).hasSize(5);
        assertThat(firstResult.getTotalElements()).isEqualTo(16); // 기존 testTodo + 15개
        assertThat(firstResult.getTotalPages()).isEqualTo(4);
        assertThat(firstResult.hasNext()).isTrue();

        // 두 번째 페이지
        Pageable secondPage = PageRequest.of(1, 5, Sort.by("id").ascending());
        TodoSearchQuery secondQuery = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                secondPage);

        Page<TodoResult> secondResult = todoService.getTodoList(secondQuery);
        assertThat(secondResult.getContent()).hasSize(5);
        assertThat(secondResult.hasNext()).isTrue();
    }

    @Test
    @DisplayName("TodoSearchQuery - 빈 리스트로 필터링")
    void getTodoListWithEmptyLists_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                Collections.emptyList(), // 빈 statusIds
                Collections.emptyList(), // 빈 categoryIds
                Collections.emptyList(), // 빈 priorityIds
                Collections.emptyList(), // 빈 tags
                null,
                Collections.emptyList(), // 빈 hideStatusIds
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
    @DisplayName("TodoSearchQuery - startDate만 있는 날짜 범위 검색")
    void getTodoListWithStartDateOnly_Success() {
        // given
        LocalDate startDate = LocalDate.now().minus(1, ChronoUnit.DAYS);
        
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                startDate,
                null, // endDate는 null
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("TodoSearchQuery - endDate만 있는 날짜 범위 검색")
    void getTodoListWithEndDateOnly_Success() {
        // given
        LocalDate endDate = LocalDate.now().plus(2, ChronoUnit.DAYS);
        
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null, // startDate는 null
                endDate,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("테스트 할일");
    }

    @Test
    @DisplayName("TodoSearchQuery - 특수문자가 포함된 키워드 검색")
    void getTodoListWithSpecialCharacterKeyword_Success() {
        // given
        Todo specialTodo = Todo.builder()
                .title("특수문자 테스트 @#$%")
                .description("특수문자 포함된 설명 !@#$%^&*()")
                .statusId(0)
                .member(testMember)
                .build();
        todoRepository.save(specialTodo);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                "@#$%", // 특수문자 키워드
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("특수문자 테스트 @#$%");
    }

    @Test
    @DisplayName("TodoSearchQuery - 존재하지 않는 memberId로 검색")
    void getTodoListWithNonExistentMemberId_Success() {
        // given
        UUID nonExistentMemberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                nonExistentMemberId,
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
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("TodoSearchQuery - 다양한 정렬 조건 테스트")
    void getTodoListWithDifferentSorting_Success() {
        // given
        Todo todo1 = Todo.builder()
                .title("A 할일")
                .description("첫 번째")
                .statusId(0)
                .priorityId(2)
                .member(testMember)
                .build();
        Todo todo2 = Todo.builder()
                .title("Z 할일")
                .description("마지막")
                .statusId(0)
                .priorityId(1)
                .member(testMember)
                .build();
        todoRepository.saveAll(List.of(todo1, todo2));

        // 제목 오름차순 정렬
        Pageable titleAscending = PageRequest.of(0, 10, Sort.by("title").ascending());
        TodoSearchQuery titleQuery = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                titleAscending);

        // when
        Page<TodoResult> titleResult = todoService.getTodoList(titleQuery);

        // then
        assertThat(titleResult.getContent()).hasSize(3);
        assertThat(titleResult.getContent().getFirst().title()).isEqualTo("A 할일");

        // 우선순위 내림차순 정렬
        Pageable priorityDescending = PageRequest.of(0, 10, Sort.by("priorityId").descending());
        TodoSearchQuery priorityQuery = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                priorityDescending);

        Page<TodoResult> priorityResult = todoService.getTodoList(priorityQuery);
        assertThat(priorityResult.getContent()).hasSize(3);
        // 우선순위가 가장 높은(2) todo1이 첫 번째에 와야 함
        assertThat(priorityResult.getContent().getFirst().title()).isEqualTo("A 할일");
    }

    @Test
    @DisplayName("TodoSearchQuery - 긴 키워드 검색 테스트")
    void getTodoListWithLongKeyword_Success() {
        // given
        String longTitle = "매우매우매우매우매우매우매우매우매우매우매우매우매우매우매우매우매우매우매우매우긴제목의할일";
        Todo longTitleTodo = Todo.builder()
                .title(longTitle)
                .description("긴 제목을 가진 할일")
                .statusId(0)
                .member(testMember)
                .build();
        todoRepository.save(longTitleTodo);

        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                "매우매우매우매우매우매우매우매우매우매우매우매우", // 긴 키워드
                null,
                null,
                null,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo(longTitle);
    }

    @Test
    @DisplayName("TodoSearchQuery - 잘못된 날짜 범위 검색 (startDate > endDate)")
    void getTodoListWithInvalidDateRange_Success() {
        // given
        LocalDate startDate = LocalDate.now().plus(2, ChronoUnit.DAYS);
        LocalDate endDate = LocalDate.now().plus(1, ChronoUnit.DAYS); // startDate보다 이전
        
        Pageable pageable = PageRequest.of(0, 10);
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null,
                null,
                null,
                null,
                null,
                null,
                startDate,
                endDate,
                pageable);

        // when
        Page<TodoResult> result = todoService.getTodoList(query);

        // then
        // 잘못된 날짜 범위로 인해 결과가 없어야 함
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 제목만 수정")
    void partialUpdateTodo_TitleOnly_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "부분 수정된 제목",
                null, // description은 수정하지 않음
                null, // statusId는 수정하지 않음
                null, null, null, null, null, null
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("부분 수정된 제목");
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명"); // 기존 값 유지
        assertThat(updatedTodo.getStatusId()).isEqualTo(0); // 기존 값 유지
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 설명만 수정")
    void partialUpdateTodo_DescriptionOnly_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, // title은 수정하지 않음
                "부분 수정된 설명",
                null, // statusId는 수정하지 않음
                null, null, null, null, null, null
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일"); // 기존 값 유지
        assertThat(updatedTodo.getDescription()).isEqualTo("부분 수정된 설명");
        assertThat(updatedTodo.getStatusId()).isEqualTo(0); // 기존 값 유지
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 상태만 수정")
    void partialUpdateTodo_StatusOnly_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, // title은 수정하지 않음
                null, // description은 수정하지 않음
                1, // statusId만 수정
                null, null, null, null, null, null
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일"); // 기존 값 유지
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명"); // 기존 값 유지
        assertThat(updatedTodo.getStatusId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 우선순위와 태그 수정")
    void partialUpdateTodo_PriorityAndTags_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, // title은 수정하지 않음
                null, // description은 수정하지 않음
                null, // statusId는 수정하지 않음
                2, // priorityId 수정
                null, null, null, null,
                Set.of("업무", "중요") // tags 수정
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일"); // 기존 값 유지
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명"); // 기존 값 유지
        assertThat(updatedTodo.getStatusId()).isEqualTo(0); // 기존 값 유지
        assertThat(updatedTodo.getPriorityId()).isEqualTo(2);
        assertThat(updatedTodo.getTags()).containsExactlyInAnyOrder("업무", "중요");
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 카테고리 수정")
    void partialUpdateTodo_CategoryOnly_Success() {
        // given
        Category newCategory = Category.builder()
                .name("새 카테고리")
                .member(testMember)
                .build();
        categoryRepository.save(newCategory);

        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                null, null, null, null,
                newCategory.getId(), // categoryId만 수정
                null, null, null, null
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일"); // 기존 값 유지
        assertThat(updatedTodo.getCategory().getName()).isEqualTo("새 카테고리");
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 빈 문자열 제목은 무시")
    void partialUpdateTodo_EmptyTitleIgnored_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "   ", // 공백만 있는 제목
                "수정된 설명",
                null, null, null, null, null, null, null
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("테스트 할일"); // 기존 값 유지 (빈 문자열 무시)
        assertThat(updatedTodo.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("Todo 부분 수정 성공 - 빈 문자열 설명은 무시")
    void partialUpdateTodo_EmptyDescriptionIgnored_Success() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                testTodo.getId(),
                "수정된 제목",
                "", // 빈 설명
                null, null, null, null, null, null, null
        );

        // when
        todoService.partialUpdateTodo(command);

        // then
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElseThrow();
        assertThat(updatedTodo.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedTodo.getDescription()).isEqualTo("테스트 설명"); // 기존 값 유지 (빈 문자열 무시)
    }

    @Test
    @DisplayName("존재하지 않는 Todo 부분 수정 시 예외 발생")
    void partialUpdateTodo_NotFound() {
        // given
        UpdateTodoCommand command = new UpdateTodoCommand(
                testMember.getId(),
                999L, // 존재하지 않는 ID
                "수정된 제목",
                null, null, null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> todoService.partialUpdateTodo(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Todo 통계 조회 성공")
    void getTodoStatistics_Success() {
        // given
        // 추가 Todo들 생성
        Todo completedTodo = Todo.builder()
                .title("완료된 할일")
                .description("완료된 설명")
                .statusId(1)
                .member(testMember)
                .build();
        
        Todo overdueTodo = Todo.builder()
                .title("지연된 할일")
                .description("지연된 설명")
                .statusId(0)
                .dueDate(LocalDate.now().minus(1, ChronoUnit.DAYS)) // 어제 마감
                .member(testMember)
                .build();
        
        todoRepository.saveAll(List.of(completedTodo, overdueTodo));

        // when
        TodoStatistics statistics = todoService.getTodoStatistics(testMember.getId());

        // then
        assertThat(statistics.total()).isEqualTo(3); // testTodo + completedTodo + overdueTodo
        assertThat(statistics.inProgress()).isEqualTo(1); // testTodo만 진행중
        assertThat(statistics.completed()).isEqualTo(1); // completedTodo
        assertThat(statistics.overdue()).isEqualTo(1); // overdueTodo가 지연됨
    }

    @Test
    @DisplayName("Todo 통계 조회 - 할일이 없는 경우")
    void getTodoStatistics_EmptyTodos_Success() {
        // given
        // 기존 testTodo 삭제
        todoRepository.delete(testTodo);

        // when
        TodoStatistics statistics = todoService.getTodoStatistics(testMember.getId());

        // then
        assertThat(statistics.total()).isEqualTo(0);
        assertThat(statistics.inProgress()).isEqualTo(0);
        assertThat(statistics.completed()).isEqualTo(0);
        assertThat(statistics.overdue()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 Todo 통계 조회")
    void getTodoStatistics_NonExistentMember_Success() {
        // given
        UUID nonExistentMemberId = UUID.randomUUID();

        // when
        TodoStatistics statistics = todoService.getTodoStatistics(nonExistentMemberId);

        // then
        assertThat(statistics.total()).isEqualTo(0);
        assertThat(statistics.inProgress()).isEqualTo(0);
        assertThat(statistics.completed()).isEqualTo(0);
        assertThat(statistics.overdue()).isEqualTo(0);
    }
}
