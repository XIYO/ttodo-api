package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.category.application.command.*;
import point.ttodoApi.category.application.mapper.CategoryApplicationMapper;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.user.application.UserQueryService;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.error.BusinessException;

import jakarta.validation.Valid;
import java.util.Optional;

/**
 * 카테고리 명령 서비스
 * TTODO 아키텍처 패턴: Command 처리 전용 서비스 (쓰기 작업)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class CategoryCommandService {
    
    private final CategoryRepository categoryRepository;
    private final UserQueryService UserQueryService; // TTODO: User Query 서비스 사용
    private final CategoryApplicationMapper mapper;

    /**
     * 카테고리 생성
     * TTODO 아키텍처 패턴: Command 기반 카테고리 생성 처리
     */
    public CategoryResult createCategory(@Valid CreateCategoryCommand command) {
        // 기존 카테고리 중복 확인
        Optional<Category> existingCategory = categoryRepository.findByNameAndOwnerId(command.name(), command.userId());
        if (existingCategory.isPresent()) {
            // TTODO 아키텍처 패턴: Application 매퍼 사용
        return mapper.toCategoryResult(existingCategory.get());
        }

        // TTODO 아키텍처 패턴: User 검증
        User user = UserQueryService.findVerifiedUser(command.userId());

        Category category = Category.builder()
                .name(command.name())
                .color(command.color())
                .description(command.description())
                .owner(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        // TTODO 아키텍처 패턴: Application 매퍼 사용
        return mapper.toCategoryResult(savedCategory);
    }

    /**
     * 카테고리 수정
     * TTODO 아키텍처 패턴: Command 기반 카테고리 수정 처리
     */
    public CategoryResult updateCategory(@Valid UpdateCategoryCommand command) {
        Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.userId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));

        // 카테고리명 중복 확인 (본인의 다른 카테고리와)
        if (!category.getName().equals(command.name()) &&
                categoryRepository.existsByNameAndOwnerId(command.name(), command.userId())) {
            throw new BusinessException("이미 존재하는 카테고리명입니다.");
        }

        category.setName(command.name());
        category.setColor(command.color());
        category.setDescription(command.description());
        
        Category savedCategory = categoryRepository.save(category);
        // TTODO 아키텍처 패턴: Application 매퍼 사용
        return mapper.toCategoryResult(savedCategory);
    }

    /**
     * 카테고리 삭제
     * TTODO 아키텍처 패턴: Command 기반 카테고리 삭제 처리
     */
    public void deleteCategory(@Valid DeleteCategoryCommand command) {
        Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.userId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));

        categoryRepository.delete(category);
    }

    // TTODO 아키텍처 패턴: Application 매퍼로 대체됨
}