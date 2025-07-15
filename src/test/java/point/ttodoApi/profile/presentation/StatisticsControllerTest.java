package point.ttodoApi.profile.presentation;
import point.ttodoApi.test.IntegrationTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.test.config.*;
import point.ttodoApi.todo.domain.*;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

import java.time.LocalDate;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StatisticsController 통계 기능 테스트
 */
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import({TestSecurityConfig.class, TestDataConfig.class})
class StatisticsControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member testMember;
    private long initialCompletedTodos;
    private long initialCategories;

    @BeforeEach
    void setUp() {
        // anon@ttodo.dev 사용자 찾기
        testMember = memberRepository.findByEmail("anon@ttodo.dev")
                .orElseThrow(() -> new RuntimeException("anon@ttodo.dev 사용자가 없습니다"));

        // 기존 데이터 카운트 저장
        initialCompletedTodos = todoRepository.countCompletedTodosByOwnerId(testMember.getId());
        initialCategories = categoryRepository.countByOwnerId(testMember.getId());

        setupTestData();
    }

    private void setupTestData() {
        LocalDate today = LocalDate.now();

        // 카테고리 생성
        Category category1 = Category.builder()
                .name("업무")
                .color("#FF0000")
                .owner(testMember)
                .build();
        categoryRepository.save(category1);

        Category category2 = Category.builder()
                .name("개인")
                .color("#00FF00")
                .owner(testMember)
                .build();
        categoryRepository.save(category2);

        // 완료된 할일들 생성
        for (int i = 1; i <= 3; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("완료된 할일 " + i)
                    .complete(true)
                    .active(true)
                    .category(category1)
                    .owner(testMember)
                    .date(today)
                    .tags(Set.of("completed"))
                    .build();
            todoRepository.save(todo);
        }

        // 미완료 할일들 생성 (통계에 포함되지 않음)
        for (int i = 4; i <= 5; i++) {
            Todo todo = Todo.builder()
                    .todoId(new TodoId((long) i, 0L))
                    .title("미완료 할일 " + i)
                    .complete(false)
                    .active(true)
                    .category(category2)
                    .owner(testMember)
                    .date(today)
                    .tags(Set.of("incomplete"))
                    .build();
            todoRepository.save(todo);
        }
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("GET /members/{memberId}/profile/statistics - 통계 조회 성공")
    void testGetStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/members/{memberId}/profile/statistics", testMember.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.completedTodos").value(initialCompletedTodos + 3))     // 기존 + 완료된 할일 3개
                .andExpect(jsonPath("$.totalCategories").value(initialCategories + 2));   // 기존 + 카테고리 2개
    }

    @Test
    @DisplayName("GET /members/{memberId}/profile/statistics - 존재하지 않는 사용자")
    void testGetStatistics_MemberNotFound() throws Exception {
        // Given
        UUID nonExistentMemberId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/members/{memberId}/profile/statistics", nonExistentMemberId.toString()))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // 400번대 에러 (정확한 코드는 구현에 따라)
    }


    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("GET /members/{memberId}/profile/statistics - JSON 응답 구조 검증")
    void testGetStatistics_ResponseStructure() throws Exception {
        // When & Then
        mockMvc.perform(get("/members/{memberId}/profile/statistics", testMember.getId().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.completedTodos").exists())
                .andExpect(jsonPath("$.completedTodos").isNumber())
                .andExpect(jsonPath("$.totalCategories").exists())
                .andExpect(jsonPath("$.totalCategories").isNumber());
    }
}