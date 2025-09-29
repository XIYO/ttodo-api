package point.ttodoApi.category.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.category.application.query.*;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.shared.error.BusinessException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * 카테고리 쿼리 서비스
 * TTODO 아키텍처 패턴: Query 처리 전용 서비스 (읽기 작업)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Validated
public class CategoryQueryService {
    
    private final CategoryRepository categoryRepository;

    /**
     * 카테고리 목록 조회 (정렬)
     * TTODO 아키텍처 패턴: Query 기반 카테고리 목록 조회
     */
    public List<CategoryResult> getCategories(@Valid CategoryListQuery query) {
        return categoryRepository.findByOwnerIdOrderByNameAsc(query.userId())
                .stream()
                .map(this::toCategoryResult)
                .toList();
    }

    /**
     * 카테고리 목록 조회 (페이징)
     * TTODO 아키텍처 패턴: Query 기반 카테고리 페이징 조회
     */
    public Page<CategoryResult> getCategories(@Valid CategoryPageQuery query) {
        return categoryRepository.findByOwnerId(query.userId(), query.pageable())
                .map(this::toCategoryResult);
    }

    /**
     * 카테고리 상세 조회
     * TTODO 아키텍처 패턴: Query 기반 카테고리 조회
     */
    public CategoryResult getCategory(@Valid CategoryQuery query) {
        Category category = categoryRepository.findByIdAndOwnerId(query.categoryId(), query.userId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
        return toCategoryResult(category);
    }

    /**
     * 카테고리 소유자 검증
     * TTODO 아키텍처 패턴: 소유권 검증 전용 메서드
     */
    public boolean isUser(UUID categoryId, UUID userId) {
        return categoryRepository.existsByIdAndOwnerId(categoryId, userId);
    }

    /**
     * 사용자의 카테고리 개수 조회
     * TTODO 아키텍처 패턴: 통계 정보 조회
     */
    public long countByOwnerId(UUID userId) {
        return categoryRepository.countByOwnerId(userId);
    }

    /**
     * 카테고리 권한 검증을 위한 엔티티 조회 (Spring Security @PreAuthorize용)
     * TTODO 아키텍처 패턴: 도메인 엔티티 직접 반환 (권한 검증 전용)
     */
    public Category findCategoryForAuth(@Valid CategoryAuthQuery query) {
        return categoryRepository.findById(query.categoryId())
                .orElseThrow(() -> new BusinessException("카테고리를 찾을 수 없습니다."));
    }

    /**
     * 카테고리 엔티티를 CategoryResult로 변환
     */
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