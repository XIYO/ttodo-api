package point.zzicback.category.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.category.application.command.*;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.category.presentation.dto.CategoryResponse;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    CategoryService.class,
    MemberService.class
})
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        CreateMemberCommand command = new CreateMemberCommand("test@example.com", "password", "tester", null);
        testMember = memberService.createMember(command);
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_Success() {
        CreateCategoryCommand command = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );

        CategoryResponse response = categoryService.createCategory(command);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("업무");
    }

    @Test
    @DisplayName("설명 없이 카테고리 생성 성공")
    void createCategory_WithoutDescription_Success() {
        CreateCategoryCommand command = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );

        CategoryResponse response = categoryService.createCategory(command);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("업무");
    }

    @Test
    @DisplayName("중복된 카테고리명으로 생성 시 기존 카테고리 반환")
    void createCategory_DuplicateName() {
        CreateCategoryCommand command1 = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );
        CategoryResponse firstResponse = categoryService.createCategory(command1);

        CreateCategoryCommand command2 = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );
        CategoryResponse secondResponse = categoryService.createCategory(command2);

        assertThat(firstResponse.id()).isEqualTo(secondResponse.id());
        assertThat(firstResponse.name()).isEqualTo(secondResponse.name());
        assertThat(firstResponse.name()).isEqualTo("업무");
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategories_Success() {
        CreateCategoryCommand command1 = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );
        CreateCategoryCommand command2 = new CreateCategoryCommand(
                testMember.getId(),
                "개인"
        );
        
        categoryService.createCategory(command1);
        categoryService.createCategory(command2);

        List<CategoryResponse> categories = categoryService.getCategories(testMember.getId());

        assertThat(categories).hasSize(2);
        assertThat(categories)
                .extracting(CategoryResponse::name)
                .containsExactly("개인", "업무");
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getCategory_Success() {
        CreateCategoryCommand command = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );
        CategoryResponse created = categoryService.createCategory(command);

        CategoryResponse response = categoryService.getCategory(testMember.getId(), created.id());

        assertThat(response.name()).isEqualTo("업무");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회 시 예외 발생")
    void getCategory_NotFound() {
        assertThatThrownBy(() -> categoryService.getCategory(testMember.getId(), 999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("카테고리를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {
        CreateCategoryCommand createCommand = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );
        CategoryResponse created = categoryService.createCategory(createCommand);

        UpdateCategoryCommand updateCommand = new UpdateCategoryCommand(
                testMember.getId(),
                created.id(),
                "회사업무"
        );

        CategoryResponse response = categoryService.updateCategory(updateCommand);

        assertThat(response.name()).isEqualTo("회사업무");
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_Success() {
        CreateCategoryCommand createCommand = new CreateCategoryCommand(
                testMember.getId(),
                "업무"
        );
        CategoryResponse created = categoryService.createCategory(createCommand);

        DeleteCategoryCommand deleteCommand = new DeleteCategoryCommand(
                testMember.getId(),
                created.id()
        );

        assertThatCode(() -> categoryService.deleteCategory(deleteCommand))
                .doesNotThrowAnyException();

        assertThat(categoryRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 예외 발생")
    void deleteCategory_NotFound() {
        DeleteCategoryCommand command = new DeleteCategoryCommand(
                testMember.getId(),
                999L
        );

        assertThatThrownBy(() -> categoryService.deleteCategory(command))
                .isInstanceOf(BusinessException.class)
                .hasMessage("카테고리를 찾을 수 없습니다.");
    }
}
