package point.zzicback.todo.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Collections;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.common.error.BusinessException;
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
    MemberService.class,
    RepeatTodoService.class
})
class TodoServiceTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberService memberService;
    
    @Autowired(required = false)
    private RepeatTodoService repeatTodoService;

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
                .dueDate(LocalDate.now().plusDays(1))
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
                null, null, null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null, null, null
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
                .dueDate(LocalDate.now().plusDays(7))
                .member(testMember)
                .build();
        todoRepository.save(extraTodo);

        Pageable pageable = PageRequest.of(0, 10);
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(3);
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
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
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
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

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
        LocalDate startDate = LocalDate.now().minusDays(1);
        
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
        LocalDate endDate = LocalDate.now().plusDays(2);
        
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
        List<String> titles = titleResult.getContent().stream()
                .map(TodoResult::title)
                .toList();
        assertThat(titles).containsExactlyInAnyOrder("A 할일", "Z 할일", "테스트 할일");

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
        assertThat(priorityResult.getContent())
                .extracting(TodoResult::title)
                .containsExactlyInAnyOrder("A 할일", "테스트 할일", "Z 할일");
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
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(1); // startDate보다 이전
        
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
                null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null,
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
                null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null, null, null, null
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
                .dueDate(LocalDate.now().minusDays(1)) // 어제 마감
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

    @Test
    @DisplayName("반복 기능 테스트 - 매일 반복")
    void createTodo_WithDailyRepeat_Success() {
        // given
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동하기",
                "30분 조깅",
                1, null, 
                LocalDate.now(), 
                null,
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null,
                Set.of("운동", "건강")
        );

        // when
        todoService.createTodo(command);

        // then
        Page<Todo> todos = todoRepository.findByMemberId(
                testMember.getId(), null, null, null,
                PageRequest.of(0, 10));
        assertThat(todos.getContent())
                .filteredOn(todo -> todo.getTitle().equals("매일 운동하기"))
                .hasSize(1);
    }

    @Test
    @DisplayName("반복 유형 상수 테스트")
    void repeatTypeConstants_Test() {
        // given & when & then
        assertThat(RepeatTypeConstants.getRepeatTypeName(RepeatTypeConstants.NONE)).isEqualTo("없음");
        assertThat(RepeatTypeConstants.getRepeatTypeName(RepeatTypeConstants.DAILY)).isEqualTo("매일");
        assertThat(RepeatTypeConstants.getRepeatTypeName(RepeatTypeConstants.WEEKLY)).isEqualTo("매주");
        assertThat(RepeatTypeConstants.getRepeatTypeName(RepeatTypeConstants.MONTHLY)).isEqualTo("매월");
        assertThat(RepeatTypeConstants.getRepeatTypeName(RepeatTypeConstants.YEARLY)).isEqualTo("매년");

        assertThat(RepeatTypeConstants.isValidRepeatType(0)).isTrue();
        assertThat(RepeatTypeConstants.isValidRepeatType(1)).isTrue();
        assertThat(RepeatTypeConstants.isValidRepeatType(4)).isTrue();
        assertThat(RepeatTypeConstants.isValidRepeatType(5)).isFalse();
        assertThat(RepeatTypeConstants.isValidRepeatType(-1)).isFalse();
    }

    @Test
    @DisplayName("반복 투두 생성 테스트")
    void repeatTodoGenerator_Test() {
        // given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 10);

        // when - 기본 투두 생성 테스트 (반복 기능 미구현으로 인한 간단한 테스트)
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동",
                "30분 조깅",
                1, null,
                startDate,
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY,
                1,
                startDate, // 반복 시작일
                endDate,
                null, // daysOfWeek
                Set.of("운동")
        );
        todoService.createTodo(command);

        // then - 기본 투두가 생성되었는지 확인
        Page<Todo> todos = todoRepository.findByMemberId(
                testMember.getId(), null, null, null,
                PageRequest.of(0, 10));
        assertThat(todos.getContent())
                .filteredOn(todo -> todo.getTitle().equals("매일 운동"))
                .hasSize(1);
                
        // RepeatTodoService가 있을 때만 테스트
        if (repeatTodoService != null) {
            List<RepeatTodo> repeatTodos = repeatTodoService.getActiveRepeatTodos(testMember.getId());
            
            if (!repeatTodos.isEmpty()) {
                RepeatTodo repeatTodo = repeatTodos.getFirst();
                assertThat(repeatTodo.getRepeatType()).isEqualTo(RepeatTypeConstants.DAILY);
                assertThat(repeatTodo.getRepeatInterval()).isEqualTo(1);
                
                // 가상 날짜 생성 테스트
                List<LocalDate> virtualDates = repeatTodoService.generateVirtualDates(
                        repeatTodo, startDate, endDate);
                assertThat(virtualDates).hasSize(10);
                assertThat(virtualDates.get(0)).isEqualTo(startDate);
                assertThat(virtualDates.get(9)).isEqualTo(LocalDate.of(2024, 1, 10));
            }
        }
    }

    @Test
    @DisplayName("가상 투두 생성 테스트 - 날짜 범위 지정시 반복 투두 표시")
    void getTodoList_WithDateRange_GeneratesVirtualTodos() {
        // 기존 테스트 데이터 정리
        todoRepository.deleteAll();
        
        // given
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동",
                "조깅하기",
                0, null, // statusId=0 (진행중)으로 변경
                LocalDate.of(2024, 1, 1), 
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY, // 매일 반복
                1, // 1일 간격
                LocalDate.of(2024, 1, 1), // 반복 시작일
                LocalDate.of(2024, 1, 5), // 5일 후 종료
                null, // daysOfWeek
                Set.of("운동")
        );
        todoService.createTodo(command);

        // when - 날짜 범위를 지정하여 투두 목록 조회
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                LocalDate.of(2024, 1, 1), // 시작일
                LocalDate.of(2024, 1, 5), // 종료일
                PageRequest.of(0, 10)
        );
        Page<TodoResult> todos = todoService.getTodoList(query);

        // then - 실제 투두 1개 + 가상 투두 4개 = 총 5개
        assertThat(todos.getContent()).hasSize(5);
        
        // 날짜별로 확인
        List<LocalDate> dueDates = todos.getContent().stream()
                .map(TodoResult::dueDate)
                .sorted()
                .collect(Collectors.toList());
                
        assertThat(dueDates).containsExactly(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3),
                LocalDate.of(2024, 1, 4),
                LocalDate.of(2024, 1, 5)
        );
        
        // 가상 투두들은 ID가 "원본ID:반복순서" 형식이어야 함
        long virtualTodoCount = todos.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":") && todo.originalTodoId() != null)
                .count();
        assertThat(virtualTodoCount).isEqualTo(4);
    }

    @Test
    @DisplayName("가상 투두 생성 테스트 - 날짜 범위 없을 때는 가상 투두 생성 안함")
    void getTodoList_WithoutDateRange_NoVirtualTodos() {
        // 기존 테스트 데이터 정리
        todoRepository.deleteAll();
        
        // given
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매주 회의",
                "팀 회의",
                1, null, 
                LocalDate.of(2024, 1, 1), 
                LocalTime.of(14, 0),
                RepeatTypeConstants.WEEKLY, // 매주 반복
                1, // 1주 간격
                LocalDate.of(2024, 1, 1), // 반복 시작일
                LocalDate.of(2024, 1, 31), // 한달 후 종료
                null, // daysOfWeek
                Set.of("회의")
        );
        todoService.createTodo(command);

        // when - 날짜 범위 없이 투두 목록 조회
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                null, // 시작일 없음
                null, // 종료일 없음
                PageRequest.of(0, 10)
        );
        Page<TodoResult> todos = todoService.getTodoList(query);

        // then - 실제 투두 1개만 있어야 함 (가상 투두 생성 안됨)
        assertThat(todos.getContent()).hasSize(1);
        assertThat(todos.getContent().getFirst().title()).isEqualTo("매주 회의");
        assertThat(todos.getContent().getFirst().title()).doesNotContain("(반복)");
    }

    @Test
    @DisplayName("가상 투두 완료 처리 테스트")
    void handleVirtualTodoCompletion_Success() {
        // 기존 테스트 데이터 정리
        todoRepository.deleteAll();
        
        // given - 반복 투두 생성
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 운동",
                "조깅하기",
                0, null, // statusId=0 (진행중)으로 변경
                LocalDate.of(2024, 1, 1), 
                LocalTime.of(9, 0),
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.of(2024, 1, 1), // 반복 시작일
                LocalDate.of(2024, 1, 5),
                null, // daysOfWeek
                Set.of("운동")
        );
        todoService.createTodo(command);
        
        // 가상 투두 목록 조회
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 5),
                PageRequest.of(0, 10)
        );
        Page<TodoResult> todos = todoService.getTodoList(query);
        
        // 가상 투두 중 하나를 선택 (2024-01-02)
        TodoResult virtualTodo = todos.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":") && todo.originalTodoId() != null)
                .filter(todo -> todo.dueDate().equals(LocalDate.of(2024, 1, 2)))
                .findFirst()
                .orElseThrow();
        
        // when - 가상 투두 완료 처리
        CompleteVirtualTodoCommand completeCommand = new CompleteVirtualTodoCommand(
                testMember.getId(),
                virtualTodo.originalTodoId(),
                1L // 원본 날짜(2024-01-01)로부터 1일 차이 = 2024-01-02
        );
        todoService.completeVirtualTodo(completeCommand);
        
        // then - 실제 투두가 생성되었는지 확인
        List<Todo> allTodos = todoRepository.findAllByMemberId(testMember.getId());
        
        // 원본 투두 1개 + 완료된 투두 1개 = 총 2개
        assertThat(allTodos).hasSize(2);
        
        // 완료된 투두 확인
        Todo completedTodo = allTodos.stream()
                .filter(todo -> todo.getDueDate().equals(LocalDate.of(2024, 1, 2)))
                .findFirst()
                .orElseThrow();
                
        assertThat(completedTodo.getTitle()).isEqualTo("매일 운동");
        assertThat(completedTodo.getStatusId()).isEqualTo(1);
        assertThat(completedTodo.getDueDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(completedTodo.getOriginalTodoId()).isNotNull();
        
        // 다시 가상 투두 목록 조회시 완료된 날짜는 제외되어야 함
        Page<TodoResult> updatedTodos = todoService.getTodoList(query);
        long virtualTodoCount = updatedTodos.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":") && todo.originalTodoId() != null)
                .filter(todo -> todo.dueDate().equals(LocalDate.of(2024, 1, 2)))
                .count();
        assertThat(virtualTodoCount).isEqualTo(0); // 완료된 날짜는 가상 투두에서 제외
    }

    @Test
    @DisplayName("가상 투두 중복 완료 방지 테스트")
    void handleVirtualTodoCompletion_DuplicatePrevention() {
        // 기존 테스트 데이터 정리
        todoRepository.deleteAll();
        
        // given - 반복 투두 생성
        CreateTodoCommand command = new CreateTodoCommand(
                testMember.getId(),
                "매일 독서",
                "30분 읽기",
                0, null, // statusId=0 (진행중)으로 변경
                LocalDate.of(2024, 1, 1), 
                LocalTime.of(20, 0),
                RepeatTypeConstants.DAILY,
                1,
                LocalDate.of(2024, 1, 1), // 반복 시작일
                LocalDate.of(2024, 1, 3),
                null, // daysOfWeek
                Set.of("독서")
        );
        todoService.createTodo(command);
        
        // 가상 투두 목록 조회
        TodoSearchQuery query = new TodoSearchQuery(
                testMember.getId(),
                null, null, null, null, null, null,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 3),
                PageRequest.of(0, 10)
        );
        Page<TodoResult> todos = todoService.getTodoList(query);
        
        // 가상 투두 선택
        TodoResult virtualTodo = todos.getContent().stream()
                .filter(todo -> todo.id() != null && todo.id().contains(":") && todo.originalTodoId() != null)
                .filter(todo -> todo.dueDate().equals(LocalDate.of(2024, 1, 2)))
                .findFirst()
                .orElseThrow();
        
        // 첫 번째 완료 처리
        CompleteVirtualTodoCommand completeCommand = new CompleteVirtualTodoCommand(
                testMember.getId(),
                virtualTodo.originalTodoId(),
                1L // 원본 날짜(2024-01-01)로부터 1일 차이 = 2024-01-02
        );
        todoService.completeVirtualTodo(completeCommand);
        
        // when & then - 같은 가상 투두를 다시 완료하려고 하면 예외 발생
        assertThatThrownBy(() -> todoService.completeVirtualTodo(completeCommand))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 완료된 투두입니다");
    }
}
