package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.*;
import point.ttodoApi.category.presentation.dto.CategorySearchRequest;
import point.ttodoApi.shared.specification.*;

import java.util.*;

/**
 * 동적 쿼리를 사용한 Category 검색 서비스 예제
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorySearchService {

  private final CategoryRepository categoryRepository;
  private final CategorySpecification categorySpecification;
  private final SortValidator sortValidator;

  /**
   * 카테고리 검색 - 다양한 조건으로 검색
   */
  public Page<Category> searchCategories(CategorySearchRequest request, Pageable pageable) {
    // 정렬 필드 검증
    sortValidator.validateSort(pageable.getSort(), categorySpecification);

    // SpecificationBuilder를 사용한 동적 쿼리 구성
    SpecificationBuilder<Category> builder = new SpecificationBuilder<>(categorySpecification);

    Specification<Category> spec = builder
            // 필수 조건
            .with("member.id", request.getMemberId())
            .with("active", true)

            // 선택적 조건들
            .withLike("title", request.getTitleKeyword())
            .withLike("colorCode", request.getColorCode())
            .withIn("shareType", request.getShareTypes())

            // 아이콘 필터링
            .withIf(request.getIconKeyword() != null,
                    "icon", request.getIconKeyword())

            // 하위 카테고리 포함 여부
            .withIf(request.isIncludeSubCategories(), builder2 ->
                    builder2.or(spec2 -> spec2.isNotNull("parentCategory")))

            .build();

    return categoryRepository.findAll(spec, pageable);
  }

  /**
   * 공개 카테고리 조회
   */
  public List<Category> getPublicCategories() {
    SpecificationBuilder<Category> builder = new SpecificationBuilder<>(categorySpecification);

    Specification<Category> spec = builder
            .with("active", true)
            .with("shareType", "PUBLIC")
            .build();

    return categoryRepository.findAll(spec);
  }

  /**
   * 특정 색상의 카테고리 조회
   */
  public List<Category> getCategoriesByColor(String colorCode, UUID memberId) {
    SpecificationBuilder<Category> builder = new SpecificationBuilder<>(categorySpecification);

    Specification<Category> spec = builder
            .with("member.id", memberId)
            .with("active", true)
            .with("colorCode", colorCode)
            .build();

    return categoryRepository.findAll(spec);
  }

}