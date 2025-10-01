package point.ttodoApi.category.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.category.application.CategoryCollaboratorService;
import point.ttodoApi.category.application.CategoryQueryService;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.domain.CategoryCollaborator;
import point.ttodoApi.category.domain.CollaboratorStatus;
import point.ttodoApi.category.presentation.dto.request.CollaboratorInviteRequest;
import point.ttodoApi.category.presentation.dto.response.CollaboratorResponse;
import point.ttodoApi.category.presentation.mapper.CategoryCollaboratorPresentationMapper;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.shared.error.ErrorCode;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.shared.security.AuthorizationService;
import point.ttodoApi.shared.security.CustomPermissionEvaluator;
import point.ttodoApi.user.domain.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({CategoryCollaboratorController.class, UserCollaborationController.class})
@Import({ApiSecurityTestConfig.class, CategoryCollaboratorControllerTest.MethodSecurityTestConfig.class})
@DisplayName("CategoryCollaboratorController 단위 테스트")
@Tag("unit")
@Tag("category")
@Tag("controller")
class CategoryCollaboratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryCollaboratorService collaboratorService;

    @MockitoBean
    private CategoryCollaboratorPresentationMapper collaboratorMapper;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    @MockitoBean
    private AuthorizationService authorizationService;

    private static final UUID CATEGORY_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID OWNER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID COLLABORATOR_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final String COLLABORATOR_USERNAME = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final String CATEGORY_BASE_PATH = "/categories/{categoryId}/collaborators";

    private Category category;
    private CategoryCollaborator invitedCollaborator;
    private CategoryCollaborator acceptedCollaborator;
    private CategoryCollaborator rejectedCollaborator;

    @BeforeEach
    void setUp() {
        User owner = User.builder()
            .id(OWNER_ID)
            .email("owner@example.com")
            .password("owner-password")
            .build();

        User collaboratorUser = User.builder()
            .id(COLLABORATOR_ID)
            .email("collab@example.com")
            .password("collab-password")
            .build();

        category = Category.builder()
            .id(CATEGORY_ID)
            .name("개인 카테고리")
            .owner(owner)
            .build();

        invitedCollaborator = new CategoryCollaborator();
        invitedCollaborator.setId(1L);
        invitedCollaborator.setCategory(category);
        invitedCollaborator.setUser(collaboratorUser);
        invitedCollaborator.setStatus(CollaboratorStatus.PENDING);
        invitedCollaborator.setInvitedAt(LocalDateTime.now());
        invitedCollaborator.setInvitationMessage("함께 해요");

        acceptedCollaborator = new CategoryCollaborator();
        acceptedCollaborator.setId(2L);
        acceptedCollaborator.setCategory(category);
        acceptedCollaborator.setUser(collaboratorUser);
        acceptedCollaborator.setStatus(CollaboratorStatus.ACCEPTED);
        acceptedCollaborator.setInvitedAt(LocalDateTime.now().minusDays(1));
        acceptedCollaborator.setAcceptedAt(LocalDateTime.now());

        rejectedCollaborator = new CategoryCollaborator();
        rejectedCollaborator.setId(3L);
        rejectedCollaborator.setCategory(category);
        rejectedCollaborator.setUser(collaboratorUser);
        rejectedCollaborator.setStatus(CollaboratorStatus.REJECTED);
        rejectedCollaborator.setInvitedAt(LocalDateTime.now());

        given(collaboratorMapper.toResponse(any(CategoryCollaborator.class))).willAnswer(invocation -> {
            CategoryCollaborator entity = invocation.getArgument(0);
            return CollaboratorResponse.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory().getId())
                .categoryName(entity.getCategory().getName())
                .userId(entity.getUser().getId())
                .userNickname("협업자")
                .userEmail(entity.getUser().getEmail())
                .status(entity.getStatus())
                .invitedAt(entity.getInvitedAt())
                .acceptedAt(entity.getAcceptedAt())
                .invitationMessage(entity.getInvitationMessage())
                .build();
        });

        given(collaboratorService.inviteCollaborator(eq(CATEGORY_ID), any(UUID.class), anyString()))
            .willReturn(invitedCollaborator);
        given(collaboratorService.acceptInvitation(eq(CATEGORY_ID), any(UUID.class)))
            .willReturn(acceptedCollaborator);
        given(collaboratorService.rejectInvitation(eq(CATEGORY_ID), any(UUID.class)))
            .willReturn(rejectedCollaborator);
        given(collaboratorService.getCategoryCollaborators(eq(CATEGORY_ID), any(UUID.class)))
            .willReturn(List.of(acceptedCollaborator));
        doNothing().when(collaboratorService).removeCollaborator(eq(CATEGORY_ID), any(UUID.class), any(UUID.class));
        doNothing().when(collaboratorService).leaveCollaboration(eq(CATEGORY_ID), any(UUID.class));

        given(collaboratorService.getPendingInvitations(any(UUID.class))).willReturn(List.of(invitedCollaborator));

        Category sharedCategory = Category.builder()
            .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
            .name("협업 카테고리")
            .owner(owner)
            .build();
        given(collaboratorService.getCollaborativeCategories(any(UUID.class))).willReturn(List.of(sharedCategory));

        given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString()))
            .willReturn(true);
    }

    @TestConfiguration
    static class MethodSecurityTestConfig {

        @Bean
        CustomPermissionEvaluator customPermissionEvaluator(AuthorizationService authorizationService) {
            return new CustomPermissionEvaluator(authorizationService);
        }

        @Bean
        MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator customPermissionEvaluator) {
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            handler.setPermissionEvaluator(customPermissionEvaluator);
            return handler;
        }
    }

    @Nested
    @DisplayName("1. 협업자 초대")
    class InviteCollaboratorTests {

        @Test
        @DisplayName("협업자 초대 성공")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void inviteCollaborator_Success() throws Exception {
            mockMvc.perform(post(CATEGORY_BASE_PATH, CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .flashAttr("collaboratorInviteRequest", new CollaboratorInviteRequest(COLLABORATOR_ID, "함께 관리해요")))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("협업자 초대 실패 - 권한 없음")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void inviteCollaborator_Forbidden() throws Exception {
            given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString()))
                .willReturn(false);

            mockMvc.perform(post(CATEGORY_BASE_PATH, CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .param("userId", COLLABORATOR_ID.toString()))
                .andExpect(status().isForbidden());

        }
    }

    @Nested
    @DisplayName("2. 초대 응답")
    class InvitationResponseTests {

        @Test
        @DisplayName("초대 수락 성공")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void acceptInvitation_Success() throws Exception {
            mockMvc.perform(post(CATEGORY_BASE_PATH + "/accept", CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("초대 거절 성공")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void rejectInvitation_Success() throws Exception {
            mockMvc.perform(post(CATEGORY_BASE_PATH + "/reject", CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("초대 수락 실패 - 인증 없음")
        void acceptInvitation_WithoutAuth() throws Exception {
            mockMvc.perform(post(CATEGORY_BASE_PATH + "/accept", CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("3. 협업자 관리")
    class CollaboratorManagementTests {

        @Test
        @DisplayName("협업자 제거 성공")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void removeCollaborator_Success() throws Exception {
            mockMvc.perform(delete(CATEGORY_BASE_PATH + "/{userId}", CATEGORY_ID, COLLABORATOR_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNoContent());

            org.mockito.Mockito.verify(collaboratorService)
                .removeCollaborator(eq(CATEGORY_ID), eq(COLLABORATOR_ID), eq(COLLABORATOR_ID));
        }

        @Test
        @DisplayName("협업 나가기 성공")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void leaveCollaboration_Success() throws Exception {
            mockMvc.perform(delete(CATEGORY_BASE_PATH, CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isNoContent());

            org.mockito.Mockito.verify(collaboratorService)
                .leaveCollaboration(CATEGORY_ID, COLLABORATOR_ID);
        }

        @Test
        @DisplayName("협업자 목록 조회 성공")
        @WithMockUser(username = COLLABORATOR_USERNAME)
        void getCollaborators_Success() throws Exception {
            mockMvc.perform(get(CATEGORY_BASE_PATH, CATEGORY_ID)
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("4. 사용자별 협업 현황")
    class UserCollaborationTests {

        @Test
        @DisplayName("대기 중 초대 목록 조회 성공")
        void getPendingInvitations_Success() throws Exception {
            mockMvc.perform(get("/user/{userId}/collaborations/invitations", COLLABORATOR_ID)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(user(testUserDetails())))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("참여 중인 협업 카테고리 조회 성공")
        void getCollaborativeCategories_Success() throws Exception {
            mockMvc.perform(get("/user/{userId}/collaborations/categories", COLLABORATOR_ID)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(user(testUserDetails())))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("참여 중인 협업 카테고리 조회 실패 - 인증 사용자 불일치")
        void getCollaborativeCategories_UnauthorizedUser() throws Exception {
            mockMvc.perform(get("/user/{userId}/collaborations/categories", COLLABORATOR_ID)
                    .with(user(new TestUserDetails(UUID.randomUUID()))))
                .andExpect(status().isForbidden());
        }
    }

    private TestUserDetails testUserDetails() {
        return new TestUserDetails(COLLABORATOR_ID);
    }

    static class TestUserDetails extends org.springframework.security.core.userdetails.User {
        private final UUID id;

        TestUserDetails(UUID id) {
            super(id.toString(), "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }
}
