package point.zzicback.category.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.application.command.*;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.category.presentation.dto.CategoryResponse;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    
    public List<CategoryResponse> getCategories(UUID memberId) {
        return categoryRepository.findByMemberIdOrderByNameAsc(memberId)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }
    
    public CategoryResponse getCategory(UUID memberId, Long categoryId) {
        Category category = categoryRepository.findByIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
        return toCategoryResponse(category);
    }
    
    @Transactional
    public CategoryResponse createCategory(CreateCategoryCommand command) {
        Optional<Category> existingCategory = categoryRepository.findByNameAndMemberId(command.name(), command.memberId());
        if (existingCategory.isPresent()) {
            return toCategoryResponse(existingCategory.get());
        }
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        Category category = Category.builder()
                .name(command.name())
                .member(member)
                .build();
                
        Category savedCategory = categoryRepository.save(category);
        return toCategoryResponse(savedCategory);
    }
    
    @Transactional
    public CategoryResponse updateCategory(UpdateCategoryCommand command) {
        Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
                
        if (!category.getName().equals(command.name()) && 
            categoryRepository.existsByNameAndMemberId(command.name(), command.memberId())) {
            throw new BusinessException("이미 존재하는 카테고리명입니다.");
        }
        
        category.updateName(command.name());
        
        return toCategoryResponse(category);
    }
    
    @Transactional
    public void deleteCategory(DeleteCategoryCommand command) {
        Category category = categoryRepository.findByIdAndMemberId(command.categoryId(), command.memberId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
                
        categoryRepository.delete(category);
    }
    
    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}
