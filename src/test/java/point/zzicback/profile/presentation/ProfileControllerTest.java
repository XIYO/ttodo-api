package point.zzicback.profile.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.test.config.TestSecurityConfig;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.profile.domain.Theme;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * ProfileController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // anon@zzic.com 사용자를 테스트 멤버로 사용
        testMember = memberService.findByEmail("anon@zzic.com")
                .orElseGet(() -> {
                    CreateMemberCommand command = new CreateMemberCommand(
                        "anon@zzic.com", 
                        "password", 
                        "익명의 찍찍이", 
                        "안녕하세요"
                    );
                    return memberService.createMember(command);
                });
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 정보 조회 성공")
    void getProfileSuccess() throws Exception {
        mockMvc.perform(get("/members/{memberId}/profile", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("익명의 찍찍이"))
                .andExpect(jsonPath("$.introduction").value(""))
                .andExpect(jsonPath("$.theme").value("PINKY"))
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 정보 수정 성공 - 닉네임과 소개글")
    void updateProfileSuccess() throws Exception {
        String requestBody = """
            {
                "nickname": "수정된닉네임",
                "introduction": "수정된 소개글입니다."
            }
            """;

        mockMvc.perform(patch("/members/{memberId}/profile", testMember.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNoContent());

        // 수정 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("수정된닉네임"))
                .andExpect(jsonPath("$.introduction").value("수정된 소개글입니다."));
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 정보 수정 성공 - 테마만")
    void updateProfileThemeOnly() throws Exception {
        String requestBody = """
            {
                "theme": "PINKY"
            }
            """;

        mockMvc.perform(patch("/members/{memberId}/profile", testMember.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNoContent());

        // 수정 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("PINKY"));
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 이미지 업로드 성공")
    void uploadProfileImageSuccess() throws Exception {
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            byte[] imageContent = imageStream.readAllBytes();
            
            MockMultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMember.getId())
                    .file(file))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 이미지 조회 - 이미지가 없는 경우")
    void getProfileImageNotFound() throws Exception {
        mockMvc.perform(get("/members/{memberId}/profile/image", testMember.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 이미지 업로드 후 조회")
    void uploadAndGetProfileImage() throws Exception {
        // 이미지 업로드
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            byte[] imageContent = imageStream.readAllBytes();
            
            MockMultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMember.getId())
                    .file(file))
                    .andExpect(status().isNoContent());
        }
        
        // 이미지 조회
        mockMvc.perform(get("/members/{memberId}/profile/image", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
        
        // 프로필 정보에서 이미지 URL 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").isNotEmpty());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 이미지 삭제")
    void deleteProfileImage() throws Exception {
        // 먼저 이미지 업로드
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            byte[] imageContent = imageStream.readAllBytes();
            
            MockMultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMember.getId())
                    .file(file))
                    .andExpect(status().isNoContent());
        }

        // 이미지 삭제
        mockMvc.perform(delete("/members/{memberId}/profile/image", testMember.getId()))
                .andExpect(status().isNoContent());
        
        // 삭제 후 조회 시 404
        mockMvc.perform(get("/members/{memberId}/profile/image", testMember.getId()))
                .andExpect(status().isNotFound());
        
        // 프로필 정보에서 이미지 URL이 null인지 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 정보 수정 - 빈 요청 본문")
    void updateProfileEmptyBody() throws Exception {
        mockMvc.perform(patch("/members/{memberId}/profile", testMember.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 정보 수정 - 너무 긴 닉네임")
    void updateProfileWithLongNickname() throws Exception {
        String longNickname = "a".repeat(300); // 255자 초과
        String requestBody = String.format("""
            {
                "nickname": "%s"
            }
            """, longNickname);

        mockMvc.perform(patch("/members/{memberId}/profile", testMember.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 정보 수정 - 너무 긴 소개글")
    void updateProfileWithLongIntroduction() throws Exception {
        String longIntroduction = "a".repeat(600); // 500자 초과
        String requestBody = String.format("""
            {
                "introduction": "%s"
            }
            """, longIntroduction);

        mockMvc.perform(patch("/members/{memberId}/profile", testMember.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}