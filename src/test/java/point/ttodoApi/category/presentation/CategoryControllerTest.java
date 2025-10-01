package point.ttodoApi.category.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.category.application.CategoryCommandService;
import point.ttodoApi.category.application.CategoryQueryService;
import point.ttodoApi.category.application.CategorySearchService;
import point.ttodoApi.category.application.command.CreateCategoryCommand;
import point.ttodoApi.category.application.command.DeleteCategoryCommand;
import point.ttodoApi.category.application.command.UpdateCategoryCommand;
import point.ttodoApi.category.application.query.CategoryPageQuery;
import point.ttodoApi.category.application.query.CategoryQuery;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.presentation.dto.request.CategorySearchRequest;
import point.ttodoApi.category.presentation.dto.request.CreateCategoryRequest;
import point.ttodoApi.category.presentation.dto.request.UpdateCategoryRequest;
import point.ttodoApi.category.presentation.dto.response.CategoryResponse;
import point.ttodoApi.category.presentation.mapper.CategoryPresentationMapper;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.shared.security.AuthorizationService;
import point.ttodoApi.shared.security.CustomPermissionEvaluator;
import point.ttodoApi.user.domain.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import({ApiSecurityTestConfig.class, CategoryControllerTest.MethodSecurityConfig.class})
@DisplayName("CategoryController 단위 테스트")
@Tag("unit")
@Tag("category")
@Tag("controller")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryCommandService categoryCommandService;

    @MockitoBean
    private CategoryQueryService categoryQueryService;

    @MockitoBean
    private CategorySearchService categorySearchService;

    @MockitoBean
    private CategoryPresentationMapper categoryPresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    @MockitoBean
    private AuthorizationService authorizationService;

    private static final String USERNAME = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final UUID USER_ID = UUID.fromString(USERNAME);
    private static final UUID CATEGORY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private CategoryResult categoryResult;
    private CategoryResponse categoryResponse;
    private Category categoryEntity;
    private CreateCategoryCommand createCommand;
    private UpdateCategoryCommand updateCommand;
    private DeleteCategoryCommand deleteCommand;
    private CategoryPageQuery pageQuery;
    private CategoryQuery categoryQuery;

    @BeforeEach
    void setUp() {
        categoryResult = new CategoryResult(
            CATEGORY_ID,
            "업무",
            "#FF0000",
            "업무용",
            0,
            Instant.now(),
            Instant.now()
        );

        categoryResponse = new CategoryResponse(
            CATEGORY_ID,
            categoryResult.name(),
            "#FF0000",
            categoryResult.description(),
            0
        );

        User owner = User.builder()
            .id(USER_ID)
            .email("user@example.com")
            .password("pw")
            .build();

        categoryEntity = Category.builder()
            .id(CATEGORY_ID)
            .name("업무")
            .color("#FF0000")
            .description("업무용")
            .owner(owner)
            .build();

        createCommand = new CreateCategoryCommand(USER_ID, "업무", "#FF0000", "업무용");
        updateCommand = new UpdateCategoryCommand(USER_ID, CATEGORY_ID, "업무", "#00FF00", "설명", 1);
        deleteCommand = new DeleteCategoryCommand(USER_ID, CATEGORY_ID);
        pageQuery = new CategoryPageQuery(USER_ID, PageRequest.of(0, 20));
        categoryQuery = new CategoryQuery(CATEGORY_ID, USER_ID);

        given(categoryPresentationMapper.toCategoryPageQuery(any(UUID.class), any(Pageable.class))).willReturn(pageQuery);
        given(categoryPresentationMapper.toCategoryQuery(any(UUID.class), any(UUID.class))).willReturn(categoryQuery);
        given(categoryPresentationMapper.toCommand(any(CreateCategoryRequest.class), any(UUID.class))).willReturn(createCommand);
        given(categoryPresentationMapper.toCommand(any(UpdateCategoryRequest.class), any(UUID.class), any(UUID.class))).willReturn(updateCommand);
        given(categoryPresentationMapper.toDeleteCommand(any(UUID.class), any(UUID.class))).willReturn(deleteCommand);
        given(categoryPresentationMapper.toResponse(any(CategoryResult.class))).willReturn(categoryResponse);
        given(categoryPresentationMapper.toResponse(any(Category.class))).willReturn(categoryResponse);

        given(categoryQueryService.getCategories(any(CategoryPageQuery.class)))
            .willReturn(new PageImpl<>(List.of(categoryResult)));
        given(categoryQueryService.getCategory(any(CategoryQuery.class))).willReturn(categoryResult);
        given(categoryCommandService.createCategory(any(CreateCategoryCommand.class))).willReturn(categoryResult);
        given(categoryCommandService.updateCategory(any(UpdateCategoryCommand.class))).willReturn(categoryResult);
        doNothing().when(categoryCommandService).deleteCategory(any(DeleteCategoryCommand.class));
        given(categorySearchService.searchCategories(any(CategorySearchRequest.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(categoryEntity)));

        given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
    }

    @TestConfiguration
    static class MethodSecurityConfig {
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
    @DisplayName("1. READ - 카테고리 조회")
    class ReadTests {

        @Test
        @DisplayName("카테고리 목록 조회 성공 - 기본 페이징")
        @WithMockUser(username = USERNAME)
        void getCategories_Default_Success() throws Exception {
            mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());

            verify(categoryQueryService).getCategories(any(CategoryPageQuery.class));
            verify(categorySearchService, never()).searchCategories(any(CategorySearchRequest.class), any(Pageable.class));
        }

        @Test
        @DisplayName("카테고리 목록 조회 성공 - 검색 조건 사용")
        @WithMockUser(username = USERNAME)
        void getCategories_WithSearchCriteria_Success() throws Exception {
            mockMvc.perform(get("/categories")
                    .param("titleKeyword", "업무"))
                .andExpect(status().isOk());

            verify(categorySearchService).searchCategories(any(CategorySearchRequest.class), any(Pageable.class));
        }

        @Test
        @DisplayName("카테고리 상세 조회 성공")
        @WithMockUser(username = USERNAME)
        void getCategory_Success() throws Exception {
            mockMvc.perform(get("/categories/" + CATEGORY_ID))
                .andExpect(status().isOk());

            verify(categoryQueryService).getCategory(any(CategoryQuery.class));
        }

        @Test
        @DisplayName("카테고리 상세 조회 실패 - 권한 없음")
        @WithMockUser(username = USERNAME)
        void getCategory_Forbidden() throws Exception {
            given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(false);

            mockMvc.perform(get("/categories/" + CATEGORY_ID))
                .andExpect(status().isForbidden());

            given(authorizationService.hasPermission(any(UUID.class), any(), anyString(), anyString())).willReturn(true);
        }
    }

    @Nested
    @DisplayName("2. CREATE - 카테고리 생성")
    class CreateTests {

        @Test
        @DisplayName("카테고리 생성 성공")
        @WithMockUser(username = USERNAME)
        void createCategory_Success() throws Exception {
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "업무")
                    .param("color", "#FF0000")
                    .param("description", "업무용"))
                .andExpect(status().isCreated());

            verify(categoryCommandService).createCategory(createCommand);
        }
    }

    @Nested
    @DisplayName("3. UPDATE - 카테고리 수정")
    class UpdateTests {

        @Test
        @DisplayName("카테고리 수정 성공")
        @WithMockUser(username = USERNAME)
        void updateCategory_Success() throws Exception {
            mockMvc.perform(put("/categories/" + CATEGORY_ID)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "업무")
                    .param("color", "#00FF00")
                    .param("description", "설명")
                    .param("orderIndex", "1"))
                .andExpect(status().isOk());

            verify(categoryCommandService).updateCategory(updateCommand);
        }
    }

    @Nested
    @DisplayName("4. DELETE - 카테고리 삭제")
    class DeleteTests {

        @Test
        @DisplayName("카테고리 삭제 성공")
        @WithMockUser(username = USERNAME)
        void deleteCategory_Success() throws Exception {
            mockMvc.perform(delete("/categories/" + CATEGORY_ID))
                .andExpect(status().isNoContent());

            verify(categoryCommandService).deleteCategory(deleteCommand);
        }
    }
}
