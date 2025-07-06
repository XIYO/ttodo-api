package point.zzicback.todo.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;
import point.zzicback.todo.domain.TodoOriginal;
import point.zzicback.todo.infrastructure.persistence.TodoOriginalRepository;
import point.zzicback.todo.presentation.dto.CreateTodoRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)  // Security filters 비활성화
@ActiveProfiles("test")
@Transactional
public class TodoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TodoOriginalRepository todoOriginalRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 생성
        testMember = Member.builder()
                .email("test@example.com")
                .nickname("테스트 사용자")
                .password("password123")
                .timeZone("Asia/Seoul")
                .locale("ko_KR")
                .build();
        
        testMember = memberRepository.save(testMember);
    }

    @Test
    @DisplayName("Todo 생성 및 조회 통합 테스트 - 타이틀만 입력")
    void createAndRetrieveTodoWithTitleOnly() throws Exception {
        // When - Todo 생성 (form data로 전송)
        mockMvc.perform(post("/todos")
                .header("Member-Id", testMember.getId().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "테스트 할일"))
                .andExpect(status().isCreated());

        // DB에서 생성된 TodoOriginal 확인
        var todos = todoOriginalRepository.findAll();
        assertEquals(1, todos.size());
        
        TodoOriginal createdTodo = todos.get(0);
        Long todoId = createdTodo.getId();
        assertNotNull(todoId);
        assertEquals("테스트 할일", createdTodo.getTitle());
        assertEquals(0, createdTodo.getRepeatType()); // 기본값 (반복 없음)
        assertEquals(1, createdTodo.getPriorityId()); // 기본값
        
        // Then - Todo 조회 (id:0 형식으로)
        mockMvc.perform(get("/todos/{id}:0", todoId)
                .header("Member-Id", testMember.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value("테스트 할일"))
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.priorityId").value(1)); // 기본값

        // 목록 조회에서도 확인
        mockMvc.perform(get("/todos")
                .header("Member-Id", testMember.getId().toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("테스트 할일"));
    }

    @Test
    @DisplayName("여러 개의 Todo 생성 후 목록 조회 테스트")
    void createMultipleTodosAndRetrieveList() throws Exception {
        // Given - 여러 개의 Todo 생성
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/todos")
                    .header("Member-Id", testMember.getId().toString())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "할일 " + i))
                    .andExpect(status().isCreated());
        }

        // When & Then - 목록 조회
        mockMvc.perform(get("/todos")
                .header("Member-Id", testMember.getId().toString())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Member-Id 헤더 없이 요청 시 실패")
    void failWithoutMemberIdHeader() throws Exception {
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "테스트 할일"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("타이틀 없이 Todo 생성 시 실패")
    void failToCreateTodoWithoutTitle() throws Exception {
        mockMvc.perform(post("/todos")
                .header("Member-Id", testMember.getId().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("날짜가 있는 Todo 생성 테스트")
    void createTodoWithDate() throws Exception {
        // 날짜가 있는 Todo 생성
        mockMvc.perform(post("/todos")
                .header("Member-Id", testMember.getId().toString())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "마감일이 있는 할일")
                .param("date", "2025-12-31"))
                .andExpect(status().isCreated());

        // DB 확인
        var todos = todoOriginalRepository.findAll();
        assertEquals(1, todos.size());
        assertNotNull(todos.get(0).getDate());
        assertEquals("2025-12-31", todos.get(0).getDate().toString());
    }
}