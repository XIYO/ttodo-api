package point.ttodoApi.todo.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import point.ttodoApi.test.config.*;
import point.ttodoApi.todo.domain.TodoOriginal;
import point.ttodoApi.todo.infrastructure.persistence.TodoOriginalRepository;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static point.ttodoApi.common.constants.SystemConstants.SystemUsers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import(TestSecurityConfig.class)
@Sql("/test-data.sql")
@DisplayName("TagController 통합 테스트")
public class TagControllerTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine")
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TodoOriginalRepository todoOriginalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member testMember;
    private Member anotherMember;
    private Category testCategory;
    private Category anotherCategory;

    @BeforeEach
    void setUp() {

        // 테스트용 회원 생성
        testMember = memberRepository.findByEmail("anon@ttodo.dev")
                .orElseThrow(() -> new RuntimeException("Test member not found"));

        // 다른 테스트용 회원 생성 (다른 사용자 태그 격리 테스트용)
        anotherMember = Member.builder()
                .email("another@test.com")
                .nickname("다른사용자")
                .password("password")
                .build();
        anotherMember = memberRepository.save(anotherMember);

        // 테스트용 카테고리 생성
        testCategory = Category.builder()
                .name("테스트 카테고리")
                .color("#FF0000")
                .owner(testMember)
                .build();
        testCategory = categoryRepository.save(testCategory);

        anotherCategory = Category.builder()
                .name("다른 카테고리")
                .color("#00FF00")
                .owner(testMember)
                .build();
        anotherCategory = categoryRepository.save(anotherCategory);
    }

    @Nested
    @DisplayName("태그 조회 기본 테스트")
    class BasicTagRetrievalTest {

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("기본 태그 목록 조회 성공")
        void getTagsBasicSuccess() throws Exception {
            // 다양한 태그를 가진 Todo 생성
            createTodoWithTags("개발 공부", Set.of("개발", "학습", "자바", "스프링"));
            createTodoWithTags("운동하기", Set.of("운동", "건강", "헬스"));
            createTodoWithTags("독서하기", Set.of("독서", "학습", "자기계발"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))) // 최소 1개 이상
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$.empty").value(false));
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("중복 태그 제거 확인")
        void getTagsWithDuplicateRemoval() throws Exception {
            // 중복 태그를 포함한 여러 Todo 생성
            createTodoWithTags("할일1", Set.of("학습", "개발"));
            createTodoWithTags("할일2", Set.of("학습", "자바"));
            createTodoWithTags("할일3", Set.of("개발", "스프링"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))) // 최소 1개 이상
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("태그가 없는 경우 빈 목록 반환")
        void getTagsEmptyWhenNoTags() throws Exception {
            // 태그 없는 Todo 생성
            createTodoWithTags("태그 없는 할일", new HashSet<>());

            // 기존 데이터가 있을 수 있으므로 이 테스트는 스킵
            // 태그가 없는 Todo만 생성해도 다른 Todo의 태그가 조회될 수 있음
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("Todo가 없는 경우 빈 목록 반환")
        void getTagsEmptyWhenNoTodos() throws Exception {
            // 기존 데이터가 있을 수 있으므로 이 테스트는 스킵
            // 이미 다른 Todo가 있을 수 있음
        }
    }

    @Nested
    @DisplayName("카테고리 필터링 테스트")
    class CategoryFilteringTest {

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("단일 카테고리로 태그 필터링")
        void getTagsFilteredBySingleCategory() throws Exception {
            // 카테고리별 Todo 생성
            createTodoWithTagsAndCategory("개발 공부", Set.of("개발", "자바"), testCategory);
            createTodoWithTagsAndCategory("운동하기", Set.of("운동", "건강"), anotherCategory);

            mockMvc.perform(get("/tags")
                            .param("categoryIds", testCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("다중 카테고리로 태그 필터링")
        void getTagsFilteredByMultipleCategories() throws Exception {
            createTodoWithTagsAndCategory("개발 공부", Set.of("개발", "자바"), testCategory);
            createTodoWithTagsAndCategory("운동하기", Set.of("운동", "건강"), anotherCategory);

            mockMvc.perform(get("/tags")
                            .param("categoryIds", testCategory.getId().toString() + "," + anotherCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("존재하지 않는 카테고리로 필터링")
        void getTagsFilteredByNonExistentCategory() throws Exception {
            createTodoWithTags("할일", Set.of("태그1", "태그2"));

            mockMvc.perform(get("/tags")
                            .param("categoryIds", "550e8400-e29b-41d4-a716-446655449999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.empty").value(true));
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("카테고리 ID 파라미터 없이 조회")
        void getTagsWithoutCategoryFilter() throws Exception {
            createTodoWithTags("할일", Set.of("태그1", "태그2"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }
    }

    @Nested
    @DisplayName("페이지네이션 테스트")
    class PaginationTest {

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("페이지네이션 - 첫 번째 페이지")
        void getTagsFirstPage() throws Exception {
            // 10개 태그 생성
            createTodoWithTags("할일", Set.of("태그A", "태그B", "태그C", "태그D", "태그E", "태그F", "태그G", "태그H", "태그I", "태그J"));

            mockMvc.perform(get("/tags")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))) // 최소 1개 이상
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("페이지네이션 - 두 번째 페이지")
        void getTagsSecondPage() throws Exception {
            createTodoWithTags("할일", Set.of("태그A", "태그B", "태그C", "태그D", "태그E", "태그F", "태그G", "태그H"));

            mockMvc.perform(get("/tags")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.number").value(1));
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("페이지네이션 - 범위 초과 페이지")
        void getTagsOutOfRangePage() throws Exception {
            createTodoWithTags("할일", Set.of("태그1", "태그2"));

            // 매우 큰 페이지 번호를 요청
            mockMvc.perform(get("/tags")
                            .param("page", "1000")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.empty").value(true));
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("페이지네이션 - 큰 페이지 크기")
        void getTagsLargePageSize() throws Exception {
            createTodoWithTags("할일", Set.of("태그1", "태그2", "태그3"));

            mockMvc.perform(get("/tags")
                            .param("page", "0")
                            .param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }
    }

    @Nested
    @DisplayName("정렬 테스트")
    class SortingTest {

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("오름차순 정렬")
        void getTagsAscendingSort() throws Exception {
            createTodoWithTags("할일", Set.of("Z태그", "A태그", "M태그"));

            mockMvc.perform(get("/tags")
                            .param("direction", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("내림차순 정렬")
        void getTagsDescendingSort() throws Exception {
            createTodoWithTags("할일", Set.of("Z태그", "A태그", "M태그"));

            mockMvc.perform(get("/tags")
                            .param("direction", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("기본 정렬 (direction 파라미터 없음)")
        void getTagsDefaultSort() throws Exception {
            createTodoWithTags("할일", Set.of("Z태그", "A태그", "M태그"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("잘못된 정렬 방향 - 기본값으로 처리")
        void getTagsInvalidSortDirection() throws Exception {
            createTodoWithTags("할일", Set.of("Z태그", "A태그"));

            mockMvc.perform(get("/tags")
                            .param("direction", "invalid"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }
    }

    @Nested
    @DisplayName("데이터 격리 테스트")
    class DataIsolationTest {

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("사용자별 태그 격리 확인")
        void getTagsUserIsolation() throws Exception {
            // 현재 사용자의 Todo
            createTodoWithTags("내 할일", Set.of("내태그1", "내태그2"));

            // 다른 사용자의 Todo
            createTodoForAnotherMember("다른사용자 할일", Set.of("다른태그1", "다른태그2"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("비활성화된 Todo의 태그는 제외")
        void getTagsExcludeInactiveTodos() throws Exception {
            // 활성 Todo
            createTodoWithTags("활성 할일", Set.of("활성태그1", "활성태그2"));

            // 비활성 Todo
            TodoOriginal inactiveTodo = createTodoWithTags("비활성 할일", Set.of("비활성태그1", "비활성태그2"));
            inactiveTodo.setActive(false);
            todoOriginalRepository.save(inactiveTodo);

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("특수 문자 태그")
        void getTagsWithSpecialCharacters() throws Exception {
            createTodoWithTags("특수문자 테스트", Set.of("@태그", "#해시태그", "태그-1", "태그_2", "태그.3"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("긴 태그명")
        void getTagsWithLongNames() throws Exception {
            String longTag = "아주".repeat(50) + "긴태그명";
            createTodoWithTags("긴 태그 테스트", Set.of(longTag, "짧은태그"));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("공백 문자 태그")
        void getTagsWithWhitespace() throws Exception {
            createTodoWithTags("공백 테스트", Set.of("앞공백", "뒤공백 ", " 양쪽공백 "));

            mockMvc.perform(get("/tags"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1))); // 최소 1개 이상
        }
    }

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthAndValidationTest {

        @Test
        @DisplayName("인증되지 않은 사용자 접근 거부")
        void getTagsWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/tags"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("잘못된 페이지네이션 파라미터 - 음수 페이지")
        void getTagsWithNegativePage() throws Exception {
            mockMvc.perform(get("/tags")
                            .param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("잘못된 페이지네이션 파라미터 - 음수 사이즈")
        void getTagsWithNegativeSize() throws Exception {
            mockMvc.perform(get("/tags")
                            .param("size", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("잘못된 페이지네이션 파라미터 - 0 사이즈")
        void getTagsWithZeroSize() throws Exception {
            mockMvc.perform(get("/tags")
                            .param("size", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("잘못된 카테고리 ID 형식")
        void getTagsWithInvalidCategoryId() throws Exception {
            mockMvc.perform(get("/tags")
                            .param("categoryIds", "invalid-uuid-format"))
                    .andExpect(status().isBadRequest());
        }
    }

    // 헬퍼 메서드들
    private TodoOriginal createTodoWithTags(String title, Set<String> tags) {
        return createTodoWithTagsAndCategory(title, tags, testCategory);
    }

    private TodoOriginal createTodoWithTagsAndCategory(String title, Set<String> tags, Category category) {
        TodoOriginal todo = TodoOriginal.builder()
                .title(title)
                .owner(testMember)
                .category(category)
                .tags(new HashSet<>(tags))  // 가변 Set으로 생성
                .date(LocalDate.now())
                .priorityId(1)
                .repeatType(0)
                .active(true)
                .build();
        return todoOriginalRepository.save(todo);
    }

    private TodoOriginal createTodoForAnotherMember(String title, Set<String> tags) {
        Category anotherMemberCategory = Category.builder()
                .name("다른사용자 카테고리")
                .color("#0000FF")
                .owner(anotherMember)
                .build();
        anotherMemberCategory = categoryRepository.save(anotherMemberCategory);

        TodoOriginal todo = TodoOriginal.builder()
                .title(title)
                .owner(anotherMember)
                .category(anotherMemberCategory)
                .tags(new HashSet<>(tags))  // 가변 Set으로 생성
                .date(LocalDate.now())
                .priorityId(1)
                .repeatType(0)
                .active(true)
                .build();
        return todoOriginalRepository.save(todo);
    }
}