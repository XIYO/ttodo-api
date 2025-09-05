package point.ttodoApi.member.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.test.BaseIntegrationTest;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.CreateMemberCommand;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static point.ttodoApi.shared.constants.SystemConstants.SystemUsers.ANON_USER_ID;

/**
 * MemberController 통합 테스트
 */
@AutoConfigureMockMvc
@Transactional
public class MemberControllerTest extends BaseIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private MemberRepository memberRepository;

    private UUID anotherMemberId;

    @BeforeEach
    void setUp() {
        // 다른 테스트 사용자 생성
        Member anotherMember = memberService.createMember(
            new CreateMemberCommand("another@example.com", "password", "다른유저", null)
        );
        anotherMemberId = anotherMember.getId();
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("본인 정보 조회 성공")
    void getMemberSuccess() throws Exception {
        mockMvc.perform(get("/members/{memberId}", ANON_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("anon@ttodo.dev"))
                .andExpect(jsonPath("$.nickname").value("익명사용자"))
                .andExpect(jsonPath("$.timeZone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.locale").value("ko-KR"));
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("다른 사용자 정보 조회 성공")
    void getMemberOtherUserSuccess() throws Exception {
        mockMvc.perform(get("/members/{memberId}", anotherMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("another@example.com"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 회원 정보 조회 시 401 에러")
    void getMemberFailureWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/members/{memberId}", ANON_USER_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("존재하지 않는 회원 조회 시 404 에러")
    void getMemberFailureWhenNotExists() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/members/{memberId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("본인 정보 수정 성공 - 모든 필드")
    void updateMemberSuccess() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", ANON_USER_ID)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "수정된닉네임")
                .param("introduction", "안녕하세요!"))
                .andExpect(status().isNoContent());

        // 수정 확인
        mockMvc.perform(get("/members/{memberId}", ANON_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("수정된닉네임"));
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("본인 정보 수정 성공 - 닉네임만")
    void updateMemberNicknameOnly() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", ANON_USER_ID)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "새닉네임"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("본인 정보 수정 - 빈 본문")
    void updateMemberEmptyBody() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", ANON_USER_ID)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("다른 사용자 정보 수정 시 403 에러")
    void updateMemberFailureWhenOtherUser() throws Exception {
        String requestBody = """
            {
                "nickname": "수정시도"
            }
            """;

        mockMvc.perform(patch("/members/{memberId}", anotherMemberId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "수정시도"))
                .andExpect(status().isForbidden());
    }








    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("회원 목록 조회 성공")
    void getMembersSuccess() throws Exception {
        mockMvc.perform(get("/members")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)));
    }


    @Test
    @DisplayName("인증되지 않은 사용자가 회원 목록 조회 시 401 에러")
    void getMembersFailureWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isUnauthorized());
    }
}