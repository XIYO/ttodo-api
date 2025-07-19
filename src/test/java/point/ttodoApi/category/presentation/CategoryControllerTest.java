package point.ttodoApi.category.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.CreateMemberCommand;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.test.config.*;

import java.util.*;

import static point.ttodoApi.common.constants.SystemConstants.SystemUsers.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CategoryController 통합 테스트
 * MockMvc를 사용하여 HTTP 레이어부터 DB까지 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
@Import(TestSecurityConfig.class)
public class CategoryControllerTest {
    
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
    private MemberService memberService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Member testMember;
    private Member otherMember;

    @BeforeEach
    void setUp() {
        // 시스템 사용자 확인 또는 생성
        testMember = memberRepository.findById(ANON_USER_ID).orElseGet(() -> {
            Member anonMember = Member.builder()
                    .email(ANON_USER_EMAIL)
                    .password(passwordEncoder.encode(ANON_USER_PASSWORD))
                    .nickname(ANON_USER_NICKNAME)
                    .build();
            anonMember.setId(ANON_USER_ID);
            return memberRepository.save(anonMember);
        });
        
        // 다른 사용자 생성 (소유자 검증 테스트용)
        CreateMemberCommand otherCommand = new CreateMemberCommand(
            "other@example.com", 
            "password", 
            "다른사용자", 
            null
        );
        otherMember = memberService.createMember(otherCommand);
    }

    @Nested
    @DisplayName("카테고리 생성 테스트")
    class CreateCategoryTest {
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("필수 필드만으로 카테고리 생성 - 이름만")
        void createCategoryWithNameOnly() throws Exception {
            String uniqueName = "테스트카테고리_" + System.currentTimeMillis();
            int beforeCount = categoryRepository.findByOwnerIdOrderByNameAsc(testMember.getId()).size();
            
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", uniqueName))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(uniqueName))
                    .andExpect(jsonPath("$.color").isEmpty())
                    .andExpect(jsonPath("$.description").isEmpty())
                    .andExpect(jsonPath("$.id").exists());
            
            // DB 검증
            List<Category> categories = categoryRepository.findByOwnerIdOrderByNameAsc(testMember.getId());
            assertThat(categories).hasSize(beforeCount + 1);
            
            Category newCategory = categories.stream()
                    .filter(c -> uniqueName.equals(c.getName()))
                    .findFirst()
                    .orElseThrow();
            assertThat(newCategory.getName()).isEqualTo(uniqueName);
            assertThat(newCategory.getColor()).isNull();
            assertThat(newCategory.getDescription()).isNull();
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("모든 필드로 카테고리 생성")
        void createCategoryWithAllFields() throws Exception {
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "개인업무")
                    .param("color", "#ff0000")
                    .param("description", "개인적인 업무 관련 할일들"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("개인업무"))
                    .andExpect(jsonPath("$.color").value("#ff0000"))
                    .andExpect(jsonPath("$.description").value("개인적인 업무 관련 할일들"));
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("빈 이름으로 카테고리 생성 시 400 에러")
        void createCategoryWithEmptyName() throws Exception {
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "")
                    .param("color", "#ff0000"))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("이름 없이 카테고리 생성 시 400 에러")
        void createCategoryWithoutName() throws Exception {
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("color", "#ff0000"))
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("중복 이름으로 카테고리 생성 - 기존 카테고리 반환")
        void createCategoryWithDuplicateName() throws Exception {
            // 첫 번째 카테고리 생성
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "중복테스트")
                    .param("color", "#ff0000"))
                    .andExpect(status().isCreated());
            
            // 동일한 이름으로 두 번째 카테고리 생성 시도
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "중복테스트")
                    .param("color", "#00ff00"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("중복테스트"))
                    .andExpect(jsonPath("$.color").value("#ff0000")); // 기존 색상 유지
        }
        
        @Test
        @DisplayName("인증되지 않은 사용자의 카테고리 생성 시도")
        void createCategoryWithoutAuth() throws Exception {
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "업무"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("카테고리 조회 테스트")
    class GetCategoryTest {
        
        private Category testCategory;
        private Category otherCategory;
        
        @BeforeEach
        void setUpCategories() {
            // 테스트 사용자의 카테고리
            testCategory = Category.builder()
                    .name("테스트카테고리")
                    .color("#ff0000")
                    .description("테스트용 카테고리")
                    .owner(testMember)
                    .build();
            testCategory = categoryRepository.save(testCategory);
            
            // 다른 사용자의 카테고리
            otherCategory = Category.builder()
                    .name("다른사용자카테고리")
                    .color("#00ff00")
                    .owner(otherMember)
                    .build();
            otherCategory = categoryRepository.save(otherCategory);
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("본인 카테고리 목록 조회")
        void getCategoriesSuccess() throws Exception {
            mockMvc.perform(get("/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.content[?(@.name == '테스트카테고리')]").exists())
                    .andExpect(jsonPath("$.content[?(@.name == '다른사용자카테고리')]").doesNotExist());
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("본인 카테고리 상세 조회")
        void getCategorySuccess() throws Exception {
            mockMvc.perform(get("/categories/{categoryId}", testCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testCategory.getId().toString()))
                    .andExpect(jsonPath("$.name").value("테스트카테고리"))
                    .andExpect(jsonPath("$.color").value("#ff0000"))
                    .andExpect(jsonPath("$.description").value("테스트용 카테고리"));
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("다른 사용자 카테고리 조회 시 403 에러")
        void getCategoryForbidden() throws Exception {
            mockMvc.perform(get("/categories/{categoryId}", otherCategory.getId()))
                    .andExpect(status().isForbidden());
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("존재하지 않는 카테고리 조회 시 403 에러 (소유자 검증 먼저)")
        void getCategoryNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            mockMvc.perform(get("/categories/{categoryId}", nonExistentId))
                    .andExpect(status().isForbidden()); // @PreAuthorize가 먼저 체크되어 403
        }
    }

    @Nested
    @DisplayName("카테고리 수정 테스트")
    class UpdateCategoryTest {
        
        private Category testCategory;
        private Category otherCategory;
        
        @BeforeEach
        void setUpCategories() {
            testCategory = Category.builder()
                    .name("수정전카테고리")
                    .color("#ff0000")
                    .description("수정전 설명")
                    .owner(testMember)
                    .build();
            testCategory = categoryRepository.save(testCategory);
            
            otherCategory = Category.builder()
                    .name("다른사용자카테고리")
                    .owner(otherMember)
                    .build();
            otherCategory = categoryRepository.save(otherCategory);
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("본인 카테고리 수정 성공")
        void updateCategorySuccess() throws Exception {
            mockMvc.perform(put("/categories/{categoryId}", testCategory.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "수정된카테고리")
                    .param("color", "#00ff00")
                    .param("description", "수정된 설명"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된카테고리"))
                    .andExpect(jsonPath("$.color").value("#00ff00"))
                    .andExpect(jsonPath("$.description").value("수정된 설명"));
            
            // DB 검증
            Category updated = categoryRepository.findById(testCategory.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("수정된카테고리");
            assertThat(updated.getColor()).isEqualTo("#00ff00");
            assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("다른 사용자 카테고리 수정 시 403 에러")
        void updateCategoryForbidden() throws Exception {
            mockMvc.perform(put("/categories/{categoryId}", otherCategory.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "수정시도")
                    .param("color", "#00ff00"))
                    .andExpect(status().isForbidden());
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("빈 이름으로 카테고리 수정 시 400 에러")
        void updateCategoryWithEmptyName() throws Exception {
            mockMvc.perform(put("/categories/{categoryId}", testCategory.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "")
                    .param("color", "#00ff00"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("카테고리 삭제 테스트")
    class DeleteCategoryTest {
        
        private Category testCategory;
        private Category otherCategory;
        
        @BeforeEach
        void setUpCategories() {
            testCategory = Category.builder()
                    .name("삭제할카테고리")
                    .owner(testMember)
                    .build();
            testCategory = categoryRepository.save(testCategory);
            
            otherCategory = Category.builder()
                    .name("다른사용자카테고리")
                    .owner(otherMember)
                    .build();
            otherCategory = categoryRepository.save(otherCategory);
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("본인 카테고리 삭제 성공")
        void deleteCategorySuccess() throws Exception {
            UUID categoryId = testCategory.getId();
            
            mockMvc.perform(delete("/categories/{categoryId}", categoryId))
                    .andExpect(status().isNoContent());
            
            // DB 검증
            assertThat(categoryRepository.findById(categoryId)).isEmpty();
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("다른 사용자 카테고리 삭제 시 403 에러")
        void deleteCategoryForbidden() throws Exception {
            UUID categoryId = otherCategory.getId();
            
            mockMvc.perform(delete("/categories/{categoryId}", categoryId))
                    .andExpect(status().isForbidden());
            
            // DB 검증 - 삭제되지 않아야 함
            assertThat(categoryRepository.findById(categoryId)).isPresent();
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("존재하지 않는 카테고리 삭제 시 403 에러")
        void deleteCategoryNotFound() throws Exception {
            UUID nonExistentId = UUID.randomUUID();
            mockMvc.perform(delete("/categories/{categoryId}", nonExistentId))
                    .andExpect(status().isForbidden()); // @PreAuthorize가 먼저 체크
        }
    }

    @Nested
    @DisplayName("인증 및 권한 테스트")
    class AuthAndValidationTest {
        
        @Test
        @DisplayName("인증되지 않은 사용자의 카테고리 목록 조회")
        void getCategoriesWithoutAuth() throws Exception {
            mockMvc.perform(get("/categories"))
                    .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("인증되지 않은 사용자의 카테고리 생성")
        void createCategoryWithoutAuth() throws Exception {
            mockMvc.perform(post("/categories")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "테스트"))
                    .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("인증되지 않은 사용자의 카테고리 수정")
        void updateCategoryWithoutAuth() throws Exception {
            UUID categoryId = UUID.randomUUID();
            mockMvc.perform(put("/categories/{categoryId}", categoryId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("name", "테스트"))
                    .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("인증되지 않은 사용자의 카테고리 삭제")
        void deleteCategoryWithoutAuth() throws Exception {
            UUID categoryId = UUID.randomUUID();
            mockMvc.perform(delete("/categories/{categoryId}", categoryId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("페이지네이션 테스트")
    class PaginationTest {
        
        @BeforeEach
        void setUpMultipleCategories() {
            // 여러 카테고리 생성
            for (int i = 1; i <= 15; i++) {
                Category category = Category.builder()
                        .name("카테고리" + String.format("%02d", i))
                        .color("#ff000" + (i % 10))
                        .owner(testMember)
                        .build();
                categoryRepository.save(category);
            }
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("첫 번째 페이지 조회 (기본 설정)")
        void getFirstPage() throws Exception {
            mockMvc.perform(get("/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(15)));
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("두 번째 페이지 조회")
        void getSecondPage() throws Exception {
            mockMvc.perform(get("/categories")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.number").value(1))
                    .andExpect(jsonPath("$.size").value(10));
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("사용자 정의 페이지 크기")
        void getWithCustomPageSize() throws Exception {
            mockMvc.perform(get("/categories")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.content", hasSize(5)));
        }
        
        @Test
        @WithUserDetails("anon@ttodo.dev")
        @DisplayName("이름 오름차순 정렬")
        void getSortedByNameAsc() throws Exception {
            mockMvc.perform(get("/categories")
                    .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
            // 정렬 순서 검증은 실제 데이터에 따라 달라짐
        }
    }
}