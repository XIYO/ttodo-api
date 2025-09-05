package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.application.command.*;
import point.ttodoApi.category.application.dto.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.domain.Member;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MemberService memberService;
    
    public List<CategoryResult> getCategories(UUID memberId) {
        return categoryRepository.findByOwnerIdOrderByNameAsc(memberId)
                .stream()
                .map(this::toCategoryResult)
                .toList();
    }
    
    public Page<CategoryResult> getCategories(UUID memberId, Pageable pageable) {
        return categoryRepository.findByOwnerId(memberId, pageable)
                .map(this::toCategoryResult);
    }
    
    public CategoryResult getCategory(UUID memberId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndOwnerId(categoryId, memberId)
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
        return toCategoryResult(category);
    }
    
    @Transactional
    public CategoryResult createCategory(CreateCategoryCommand command) {
        Optional<Category> existingCategory = categoryRepository.findByNameAndOwnerId(command.name(), command.memberId());
        if (existingCategory.isPresent()) {
            return toCategoryResult(existingCategory.get());
        }
        
        Member member = memberService.findByIdOrThrow(command.memberId());
        
        Category category = Category.builder()
                .name(command.name())
                .color(command.color())
                .description(command.description())
                .owner(member)
                .build();
                
        Category savedCategory = categoryRepository.save(category);
        return toCategoryResult(savedCategory);
    }
    
    @Transactional
    public CategoryResult updateCategory(UpdateCategoryCommand command) {
        Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.memberId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
                
        if (!category.getName().equals(command.name()) &&
            categoryRepository.existsByNameAndOwnerId(command.name(), command.memberId())) {
            throw new BusinessException("이미 존재하는 카테고리명입니다.");
        }

        category.update(command.name(), command.color(), command.description());
        
        return toCategoryResult(category);
    }
    
    @Transactional
    public void deleteCategory(DeleteCategoryCommand command) {
        Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.memberId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
                
        categoryRepository.delete(category);
    }
    
    /**
     * 카테고리 소유자 검증
     * @param categoryId 카테고리 ID
     * @param memberId 회원 ID
     * @return 소유자 여부
     */
    public boolean isMember(UUID categoryId, UUID memberId) {
        return categoryRepository.existsByIdAndOwnerId(categoryId, memberId);
    }
    
    /**
     * 사용자의 카테고리 개수 조회
     * @param memberId 회원 ID
     * @return 카테고리 개수
     */
    public long countByOwnerId(UUID memberId) {
        return categoryRepository.countByOwnerId(memberId);
    }
    
    /**
     * 카테고리 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
     * @param categoryId 카테고리 ID
     * @return 카테고리 엔티티
     */
    public Category findCategoryForAuth(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
    }
    
    private CategoryResult toCategoryResult(Category category) {
        return new CategoryResult(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getDescription(),
                null, // displayOrder는 현재 Category 엔티티에 없음
                null, // createdAt은 BaseEntity에서 상속받을 예정
                null  // updatedAt은 BaseEntity에서 상속받을 예정
        );
    }
}
