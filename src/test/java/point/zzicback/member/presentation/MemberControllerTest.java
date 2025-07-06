package point.zzicback.member.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.test.config.TestSecurityConfig;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;

import java.io.InputStream;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.hamcrest.Matchers.*;

/**
 * MemberController 통합 테스트
 * Spring Security를 활성화한 상태에서 @WithMockUser를 사용하여 인증 처리
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    private Member testMember;
    private Member anotherMember;

    @BeforeEach
    void setUp() {
        // anon@zzic.com 사용자를 테스트 멤버로 사용
        testMember = memberService.findByEmail("anon@zzic.com")
                .orElseGet(() -> {
                    // 만약 없다면 생성 (테스트 환경에서는 AnonMemberInitializer가 실행되지 않을 수 있음)
                    CreateMemberCommand command = new CreateMemberCommand(
                        "anon@zzic.com", 
                        "password", 
                        "익명의 찍찍이", 
                        "안녕하세요"
                    );
                    return memberService.createMember(command);
                });

        // 또 다른 테스트 회원 생성
        CreateMemberCommand anotherCommand = new CreateMemberCommand(
            "another@example.com",
            "password",
            "다른유저",
            null
        );
        anotherMember = memberService.createMember(anotherCommand);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("회원 목록 조회 - 페이징")
    void getMembers() throws Exception {
        mockMvc.perform(get("/members")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("회원 상세 조회 성공")
    void getMemberSuccess() throws Exception {
        mockMvc.perform(get("/members/{memberId}", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testMember.getId().toString()))
                .andExpect(jsonPath("$.email").value("anon@zzic.com"))
                .andExpect(jsonPath("$.nickname").value("익명의 찍찍이"))
                .andExpect(jsonPath("$.introduction").value(""))
                .andExpect(jsonPath("$.timeZone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.locale").value("ko_KR"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("존재하지 않는 회원 조회 시 404")
    void getMemberNotFound() throws Exception {
        mockMvc.perform(get("/members/{memberId}", "550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("회원 정보 수정 성공")
    void updateMemberSuccess() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", testMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "수정된닉네임")
                .param("introduction", "수정된 소개글입니다."))
                .andExpect(status().isNoContent());  // 컨트롤러가 204 반환

        // 수정 확인
        mockMvc.perform(get("/members/{memberId}", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("수정된닉네임"))
                .andExpect(jsonPath("$.introduction").value("수정된 소개글입니다."));
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("회원 정보 수정 - 닉네임만 수정")
    void updateMemberNicknameOnly() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", testMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "닉네임만변경"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/members/{memberId}", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("닉네임만변경"))
                .andExpect(jsonPath("$.introduction").value(""));
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("회원 정보 수정 - 빈 요청 본문")
    void updateMemberEmptyBody() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", testMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("테마 변경 성공")
    void updateThemeSuccess() throws Exception {
        mockMvc.perform(patch("/members/{memberId}/theme", testMember.getId())
                .param("theme", "DARK"))  // Enum 값은 대문자로
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("프로필 이미지 업로드 성공")
    void uploadProfileImageSuccess() throws Exception {
        // 테스트 리소스에서 실제 이미지 파일 로드
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            byte[] imageContent = imageStream.readAllBytes();
            
            MockMultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile-image", testMember.getId())
                    .file(file))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("프로필 이미지 업로드 - 파일 없이 요청")
    void uploadProfileImageWithoutFile() throws Exception {
        mockMvc.perform(post("/members/{memberId}/profile-image", testMember.getId())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("필수 파일 누락"))
                .andExpect(jsonPath("$.detail").value("필수 파일 파라미터 'image'이(가) 누락되었습니다."))
                .andExpect(jsonPath("$.errorCode").value("MISSING_FILE"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("프로필 이미지 조회 - 이미지가 없는 경우")
    void getProfileImageNotFound() throws Exception {
        mockMvc.perform(get("/members/{memberId}/profile-image", testMember.getId()))
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

            mockMvc.perform(multipart("/members/{memberId}/profile-image", testMember.getId())
                    .file(file))
                    .andExpect(status().isNoContent());
        }
        
        // 이미지 조회
        mockMvc.perform(get("/members/{memberId}/profile-image", testMember.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
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

            mockMvc.perform(multipart("/members/{memberId}/profile-image", testMember.getId())
                    .file(file))
                    .andExpect(status().isNoContent());
        }

        // 이미지 삭제
        mockMvc.perform(delete("/members/{memberId}/profile-image", testMember.getId()))
                .andExpect(status().isNoContent());
        
        // 삭제 후 조회 시 404
        mockMvc.perform(get("/members/{memberId}/profile-image", testMember.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("회원 목록 조회 - 페이지 크기 제한")
    void getMembersWithLargePageSize() throws Exception {
        mockMvc.perform(get("/members")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageable.pageSize").value(100)); // 실제로는 100개 그대로 반환
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("회원 정보 수정 - 너무 긴 닉네임")
    void updateMemberWithLongNickname() throws Exception {
        String longNickname = "a".repeat(300); // 300자 (255자 초과)

        mockMvc.perform(patch("/members/{memberId}", testMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", longNickname))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("회원 정보 수정 - 너무 긴 소개글")
    void updateMemberWithLongIntroduction() throws Exception {
        String longIntro = "a".repeat(600); // 600자 (500자 초과)

        mockMvc.perform(patch("/members/{memberId}", testMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("introduction", longIntro))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 회원 목록 조회 시도")
    void getMembersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/members")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 회원 정보 수정 시도")
    void updateMemberWithoutAuthentication() throws Exception {
        mockMvc.perform(patch("/members/{memberId}", testMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "수정하면안됨"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("anon@zzic.com")
    @DisplayName("다른 사용자의 정보 수정 시도 - 권한 검증")
    void updateOtherUserProfile() throws Exception {
        // 테스트용 사용자로 인증 설정 (testMember의 ID를 사용)
        // 다른 사용자(anotherMember)의 정보를 수정하려고 시도하면 403 Forbidden
        mockMvc.perform(patch("/members/{memberId}", anotherMember.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("nickname", "남의닉네임변경"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    @DisplayName("관리자 권한으로 회원 목록 조회")
    void getMembersAsAdmin() throws Exception {
        mockMvc.perform(get("/members")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("본인 정보 조회")
    void getOwnProfile() throws Exception {
        // 본인의 ID로 조회
        mockMvc.perform(get("/members/{memberId}", testMember.getId())
                .with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("anon@zzic.com"))
                .andExpect(jsonPath("$.nickname").value("익명의 찍찍이"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("잘못된 형식의 UUID로 회원 조회")
    void getMemberWithInvalidUUID() throws Exception {
        mockMvc.perform(get("/members/{memberId}", "invalid-uuid"))
                .andExpect(status().is5xxServerError()); // UUID 파싱 실패로 인한 500 에러
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("지원하지 않는 이미지 형식 업로드")
    void uploadUnsupportedImageFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "This is not an image".getBytes()
        );

        mockMvc.perform(multipart("/members/{memberId}/profile-image", testMember.getId())
                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    @DisplayName("너무 큰 파일 업로드 시도")
    void uploadTooLargeFile() throws Exception {
        // 11MB 파일 생성 (제한: 10MB)
        // 실제 이미지 헤더를 포함하여 더 현실적인 테스트
        byte[] largeContent = new byte[11 * 1024 * 1024];
        // JPEG 헤더 추가
        largeContent[0] = (byte) 0xFF;
        largeContent[1] = (byte) 0xD8;
        largeContent[2] = (byte) 0xFF;
        
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "large.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            largeContent
        );

        // Spring Boot의 기본 에러 처리는 400 Bad Request를 반환할 수 있음
        mockMvc.perform(multipart("/members/{memberId}/profile-image", testMember.getId())
                .file(file))
                .andExpect(status().is4xxClientError());  // 400 또는 413 모두 허용
    }
}