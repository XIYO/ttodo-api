package point.zzicback.category.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.category.domain.Category;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByMemberIdOrderByNameAsc(UUID memberId);
    Page<Category> findByMemberId(UUID memberId, Pageable pageable);
    Optional<Category> findByIdAndMemberId(Long id, UUID memberId);
    boolean existsByNameAndMemberId(String name, UUID memberId);
    boolean existsByIdAndMemberId(Long id, UUID memberId);
    Optional<Category> findByNameAndMemberId(String name, UUID memberId);
    
    // 통계용 메서드
    long countByMemberId(UUID memberId);
}
