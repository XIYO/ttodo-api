package point.ttodoApi.profile.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.test.config.*;

import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProfileController 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig.class)
@Sql("/test-data.sql")
public class ProfileControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine")
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    private UUID testMemberId;

    @BeforeEach
    void setUp() {
        testMemberId = memberRepository.findByEmail("anon@ttodo.dev").get().getId();
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 정보 조회 성공")
    void getProfileSuccess() throws Exception {
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("익명사용자"))
                .andExpect(jsonPath("$.theme").value("PINKY"))
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 정보 수정 성공 - 닉네임과 소개글")
    void updateProfileSuccess() throws Exception {
        String requestBody = """
            {
                "nickname": "수정된닉네임",
                "introduction": "수정된 소개글입니다."
            }
            """;

        mockMvc.perform(patch("/members/{memberId}/profile", testMemberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNoContent());

        // 수정 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("수정된닉네임"))
                .andExpect(jsonPath("$.introduction").value("수정된 소개글입니다."));
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 정보 수정 성공 - 테마만")
    void updateProfileThemeOnly() throws Exception {
        String requestBody = """
            {
                "theme": "PINKY"
            }
            """;

        mockMvc.perform(patch("/members/{memberId}/profile", testMemberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNoContent());

        // 수정 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("PINKY"));
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
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

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
        }
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 이미지 조회 - 이미지가 없는 경우")
    void getProfileImageNotFound() throws Exception {
        mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
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

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
        }
        
        // 이미지 조회
        mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
        
        // 프로필 정보에서 이미지 URL 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
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

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(file))
                    .andExpect(status().isOk());
        }

        // 이미지 삭제
        mockMvc.perform(delete("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isNoContent());
        
        // 삭제 후 조회 시 404
        mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isNotFound());
        
        // 프로필 정보에서 이미지 URL이 null인지 확인
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 정보 수정 - 빈 요청 본문")
    void updateProfileEmptyBody() throws Exception {
        mockMvc.perform(patch("/members/{memberId}/profile", testMemberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 정보 수정 - 너무 긴 닉네임")
    void updateProfileWithLongNickname() throws Exception {
        String longNickname = "a".repeat(300); // 255자 초과
        String requestBody = String.format("""
            {
                "nickname": "%s"
            }
            """, longNickname);

        mockMvc.perform(patch("/members/{memberId}/profile", testMemberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 정보 수정 - 너무 긴 소개글")
    void updateProfileWithLongIntroduction() throws Exception {
        String longIntroduction = "a".repeat(600); // 500자 초과
        String requestBody = String.format("""
            {
                "introduction": "%s"
            }
            """, longIntroduction);

        mockMvc.perform(patch("/members/{memberId}/profile", testMemberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 이미지 업로드 → URL 반환 → URL로 이미지 다운로드 전체 시나리오")
    void uploadImageAndDownloadWithReturnedUrl() throws Exception {
        // 1. 이미지 업로드하고 URL 받기
        String returnedImageUrl;
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            byte[] originalImageContent = imageStream.readAllBytes();
            
            MockMultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                originalImageContent
            );

            // 업로드 요청 후 반환된 URL 추출
            String response = mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").exists())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            
            // JSON에서 imageUrl 추출
            returnedImageUrl = response.split("\"imageUrl\":\"")[1].split("\"")[0];
            
            // 2. 반환된 URL로 이미지 다운로드
            byte[] downloadedImageContent = mockMvc.perform(get(returnedImageUrl))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                    .andExpect(header().string("Cache-Control", "public, max-age=3600"))
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
            
            // 3. 다운로드한 이미지와 원본 비교
            assertThat(downloadedImageContent).isEqualTo(originalImageContent);
            
            // 4. 프로필 조회 시에도 같은 URL이 반환되는지 확인
            mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value(returnedImageUrl));
        }
    }
    
    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 이미지 삭제 시나리오 - 업로드 → 삭제 → 조회 실패")
    void deleteProfileImageScenario() throws Exception {
        // 1. 먼저 이미지 업로드
        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            byte[] imageContent = imageStream.readAllBytes();
            
            MockMultipartFile file = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                imageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
        }
        
        // 2. 업로드 확인 - 이미지 조회 가능
        mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE));
        
        // 3. 프로필 조회 시 imageUrl 있음
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
        
        // 4. 이미지 삭제
        mockMvc.perform(delete("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isNoContent());
        
        // 5. 삭제 후 이미지 조회 시 404
        mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                .andExpect(status().isNotFound());
        
        // 6. 프로필 조회 시 imageUrl이 null
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").isEmpty());
    }
    
    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 이미지 다중 업로드 - 두 번째 이미지로 교체")
    void uploadProfileImageTwice() throws Exception {
        // 1. 첫 번째 이미지(profile.jpg) 업로드
        byte[] firstImageContent;
        try (InputStream firstImageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            firstImageContent = firstImageStream.readAllBytes();
            
            MockMultipartFile firstFile = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                firstImageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(firstFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
            
            // 첫 번째 이미지 다운로드하여 확인
            byte[] downloadedFirstImage = mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
            
            assertThat(downloadedFirstImage).isEqualTo(firstImageContent);
        }
        
        // 2. 두 번째 이미지(profile2.png) 업로드
        byte[] secondImageContent;
        try (InputStream secondImageStream = getClass().getClassLoader().getResourceAsStream("profile2.png")) {
            secondImageContent = secondImageStream.readAllBytes();
            
            MockMultipartFile secondFile = new MockMultipartFile(
                "image",
                "profile2.png",
                MediaType.IMAGE_PNG_VALUE,
                secondImageContent
            );

            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(secondFile))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
            
            // 두 번째 이미지 다운로드하여 확인
            byte[] downloadedSecondImage = mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_PNG_VALUE))
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
            
            // 두 번째 이미지로 교체되었는지 확인
            assertThat(downloadedSecondImage).isEqualTo(secondImageContent);
            // 첫 번째 이미지와 다른지 확인 (실제 바이트 코드가 바뀌었는지)
            assertThat(downloadedSecondImage).isNotEqualTo(firstImageContent);
        }
        
        // 3. 프로필 조회 시에도 같은 URL 유지
        mockMvc.perform(get("/members/{memberId}/profile", testMemberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/members/" + testMemberId + "/profile/image"));
    }
    
    @Test
    @WithUserDetails("anon@ttodo.dev")
    @DisplayName("프로필 이미지 교체 시 실제 바이트 코드 변경 확인")
    void verifyImageBytesChangeAfterUpdate() throws Exception {
        // 1. 첫 번째 이미지(profile.jpg) 업로드 및 저장
        byte[] firstImageOriginal;
        byte[] firstImageDownloaded;
        
        try (InputStream firstImageStream = getClass().getClassLoader().getResourceAsStream("profile.jpg")) {
            firstImageOriginal = firstImageStream.readAllBytes();
            
            MockMultipartFile firstFile = new MockMultipartFile(
                "image",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                firstImageOriginal
            );

            // 업로드
            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(firstFile))
                    .andExpect(status().isOk());
            
            // 다운로드하여 저장
            firstImageDownloaded = mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
            
            // 원본과 다운로드한 이미지가 같은지 확인
            assertThat(firstImageDownloaded).isEqualTo(firstImageOriginal);
        }
        
        // 2. 두 번째 이미지(profile2.png) 업로드
        byte[] secondImageOriginal;
        byte[] secondImageDownloaded;
        
        try (InputStream secondImageStream = getClass().getClassLoader().getResourceAsStream("profile2.png")) {
            secondImageOriginal = secondImageStream.readAllBytes();
            
            MockMultipartFile secondFile = new MockMultipartFile(
                "image",
                "profile2.png",
                MediaType.IMAGE_PNG_VALUE,
                secondImageOriginal
            );

            // 업로드
            mockMvc.perform(multipart("/members/{memberId}/profile/image", testMemberId)
                    .file(secondFile))
                    .andExpect(status().isOk());
            
            // 다운로드
            secondImageDownloaded = mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
        }
        
        // 3. 검증
        // 두 번째 이미지가 원본과 같은지 확인
        assertThat(secondImageDownloaded).isEqualTo(secondImageOriginal);
        
        // 이전 이미지와 현재 이미지가 다른지 확인 (실제 바이트 코드가 변경되었는지)
        assertThat(secondImageDownloaded).isNotEqualTo(firstImageDownloaded);
        
        // 크기도 다른지 확인 (참고용)
        assertThat(secondImageDownloaded.length).isNotEqualTo(firstImageDownloaded.length);
        
        // 같은 URL로 여러 번 요청해도 같은 이미지가 나오는지 확인
        for (int i = 0; i < 3; i++) {
            byte[] repeatedDownload = mockMvc.perform(get("/members/{memberId}/profile/image", testMemberId))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray();
            
            assertThat(repeatedDownload).isEqualTo(secondImageDownloaded);
        }
    }
}