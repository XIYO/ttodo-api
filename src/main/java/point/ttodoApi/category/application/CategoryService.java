package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.application.command.*;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.error.BusinessException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final UserService UserService;

  public List<CategoryResult> getCategories(UUID userId) {
    return categoryRepository.findByOwnerIdOrderByNameAsc(userId)
            .stream()
            .map(this::toCategoryResult)
            .toList();
  }

  public Page<CategoryResult> getCategories(UUID userId, Pageable pageable) {
    return categoryRepository.findByOwnerId(userId, pageable)
            .map(this::toCategoryResult);
  }

  public CategoryResult getCategory(UUID userId, UUID categoryId) {
    Category category = categoryRepository.findByIdAndOwnerId(categoryId, userId)
            .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
    return toCategoryResult(category);
  }

  @Transactional
  public CategoryResult createCategory(CreateCategoryCommand command) {
    Optional<Category> existingCategory = categoryRepository.findByNameAndOwnerId(command.name(), command.userId());
    if (existingCategory.isPresent()) {
      return toCategoryResult(existingCategory.get());
    }

    User user = UserService.findByIdOrThrow(command.userId());

    Category category = Category.builder()
            .name(command.name())
            .color(command.color())
            .description(command.description())
            .owner(user)
            .build();

    Category savedCategory = categoryRepository.save(category);
    return toCategoryResult(savedCategory);
  }

  @Transactional
  public CategoryResult updateCategory(UpdateCategoryCommand command) {
    Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.userId())
            .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));

    if (!category.getName().equals(command.name()) &&
            categoryRepository.existsByNameAndOwnerId(command.name(), command.userId())) {
      throw new BusinessException("이미 존재하는 카테고리명입니다.");
    }

    category.setName(command.name());
    category.setColor(command.color());
    category.setDescription(command.description());
    if (command.orderIndex() != null) {
      category.setOrderIndex(command.orderIndex());
    }

    return toCategoryResult(category);
  }

  @Transactional
  public void deleteCategory(DeleteCategoryCommand command) {
    Category category = categoryRepository.findByIdAndOwnerId(command.categoryId(), command.userId())
            .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));

    categoryRepository.delete(category);
  }

  /**
   * 카테고리 소유자 검증
   *
   * @param categoryId 카테고리 ID
   * @param userId   회원 ID
   * @return 소유자 여부
   */
  public boolean isUser(UUID categoryId, UUID userId) {
    return categoryRepository.existsByIdAndOwnerId(categoryId, userId);
  }

  /**
   * 사용자의 카테고리 개수 조회
   *
   * @param userId 회원 ID
   * @return 카테고리 개수
   */
  public long countByOwnerId(UUID userId) {
    return categoryRepository.countByOwnerId(userId);
  }

  /**
   * 카테고리 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
   *
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
            category.getOrderIndex(),
            category.getCreatedAt(),
            category.getUpdatedAt()
    );
  }
}
