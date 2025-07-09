package point.zzicback.profile.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;
import point.zzicback.todo.infrastructure.persistence.TodoRepository;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StatisticsController 통계 기능 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatisticsControllerTest {

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

    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        testMember = Member.builder()
                .email("statistics@test.com")
                .nickname("통계테스트")
                .password("password123!")
                .build();
        testMember = memberRepository.save(testMember);

        setupTestData();
    }

    private void setupTestData() {
        LocalDate today = LocalDate.now();

        // 카테고리 생성
        Category category1 = Category.builder()
                .name("업무")
                .color("#FF0000")
                .member(testMember)
                .build();
        categoryRepository.save(category1);

        Category category2 = Category.builder()
                .name("개인")
                .color("#00FF00")
                .member(testMember)
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
                    .member(testMember)
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
                    .member(testMember)
                    .date(today)
                    .tags(Set.of("incomplete"))
                    .build();
            todoRepository.save(todo);
        }
    }

    @Test
    @DisplayName("GET /members/{memberId}/statistics - 통계 조회 성공")
    void testGetStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/members/{memberId}/statistics", testMember.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.completedTodos").value(3))     // 완료된 할일 3개
                .andExpect(jsonPath("$.totalCategories").value(2));   // 카테고리 2개
    }

    @Test
    @DisplayName("GET /members/{memberId}/statistics - 존재하지 않는 사용자")
    void testGetStatistics_MemberNotFound() throws Exception {
        // Given
        String nonExistentMemberId = "11111111-1111-1111-1111-111111111111";

        // When & Then
        mockMvc.perform(get("/members/{memberId}/statistics", nonExistentMemberId))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // 400번대 에러 (정확한 코드는 구현에 따라)
    }

    @Test
    @DisplayName("GET /members/{memberId}/statistics - 데이터가 없는 사용자")
    void testGetStatistics_EmptyData() throws Exception {
        // Given - 데이터가 없는 새 사용자 생성
        Member newMember = Member.builder()
                .email("newuser@test.com")
                .nickname("신규사용자")
                .password("password123!")
                .build();
        newMember = memberRepository.save(newMember);

        // When & Then
        mockMvc.perform(get("/members/{memberId}/statistics", newMember.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.completedTodos").value(0))     // 완료된 할일 0개
                .andExpect(jsonPath("$.totalCategories").value(0));   // 카테고리 0개
    }

    @Test
    @DisplayName("GET /members/{memberId}/statistics - JSON 응답 구조 검증")
    void testGetStatistics_ResponseStructure() throws Exception {
        // When & Then
        mockMvc.perform(get("/members/{memberId}/statistics", testMember.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.completedTodos").exists())
                .andExpect(jsonPath("$.completedTodos").isNumber())
                .andExpect(jsonPath("$.totalCategories").exists())
                .andExpect(jsonPath("$.totalCategories").isNumber());
    }
}