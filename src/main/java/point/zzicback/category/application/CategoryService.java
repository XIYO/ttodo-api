package point.zzicback.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.category.application.command.*;
import point.zzicback.category.domain.Category;
import point.zzicback.category.infrastructure.CategoryRepository;
import point.zzicback.category.presentation.dto.CategoryResponse;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.util.*;

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
    
    public Page<CategoryResponse> getCategories(UUID memberId, Pageable pageable) {
        return categoryRepository.findByMemberId(memberId, pageable)
                .map(this::toCategoryResponse);
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
                .color(command.color())
                .description(command.description())
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

        category.update(command.name(), command.color(), command.description());
        
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
                category.getName(),
                category.getColor(),
                category.getDescription()
        );
    }
}
