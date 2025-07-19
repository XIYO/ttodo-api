package point.ttodoApi.profile.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
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
import static point.ttodoApi.common.constants.SystemConstants.SystemUsers.*;

/**
 * StatisticsController 통계 기능 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig.class)
class StatisticsControllerTest {

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
    private TodoRepository todoRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        Member testMember = memberRepository.findById(ANON_USER_ID).get();
        
        // 테스트용 카테고리 생성
        testCategory = categoryRepository.save(
            Category.builder()
                .name("테스트 카테고리")
                .color("#FF0000")
                .owner(testMember)
                .build()
        );
    }

    private void createTestTodos() {
        Member testMember = memberRepository.findById(ANON_USER_ID).get();
        LocalDate today = LocalDate.now();
        
        // 완료된 Todo
        for (int i = 0; i < 3; i++) {
            todoRepository.save(
                Todo.builder()
                    .todoId(new TodoId((long)(i + 1), 0L))
                    .title("완료된 할 일 " + (i + 1))
                    .complete(true)
                    .date(today)
                    .owner(testMember)
                    .category(testCategory)
                    .build()
            );
        }
        
        // 미완료 Todo
        for (int i = 0; i < 2; i++) {
            todoRepository.save(
                Todo.builder()
                    .todoId(new TodoId((long)(i + 4), 0L))
                    .title("미완료 할 일 " + (i + 1))
                    .complete(false)
                    .date(today)
                    .owner(testMember)
                    .build()
            );
        }
        
        // 어제 완료된 Todo
        todoRepository.save(
            Todo.builder()
                .todoId(new TodoId(6L, 0L))
                .title("어제 완료한 할 일")
                .complete(true)
                .date(today.minusDays(1))
                .owner(testMember)
                .build()
        );
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("GET /members/{memberId}/profile/statistics - 통계 조회 성공")
    void testGetStatistics_Success() throws Exception {
        // given
        createTestTodos();
        
        // when & then
        mockMvc.perform(get("/members/{memberId}/profile/statistics", ANON_USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedTodos").value(4))
                .andExpect(jsonPath("$.totalCategories").value(2)); // TodoInitializer가 생성한 기본 카테고리 1개 + 테스트 카테고리 1개
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")  
    @DisplayName("GET /members/{memberId}/profile/statistics - JSON 응답 구조 검증")
    void testGetStatistics_ResponseStructure() throws Exception {
        // given
        createTestTodos();
        
        // when & then
        mockMvc.perform(get("/members/{memberId}/profile/statistics", ANON_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedTodos").isNumber())
                .andExpect(jsonPath("$.totalCategories").isNumber());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 통계 조회 시 401")
    void testGetStatistics_Unauthorized() throws Exception {
        mockMvc.perform(get("/members/{memberId}/profile/statistics", ANON_USER_ID))
                .andExpect(status().isUnauthorized());
    }
}