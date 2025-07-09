package point.ttodoApi.category.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import point.ttodoApi.category.domain.Category;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByMemberIdOrderByNameAsc(UUID memberId);
    Page<Category> findByMemberId(UUID memberId, Pageable pageable);
    Optional<Category> findByIdAndMemberId(UUID id, UUID memberId);
    boolean existsByNameAndMemberId(String name, UUID memberId);
    boolean existsByIdAndMemberId(UUID id, UUID memberId);
    Optional<Category> findByNameAndMemberId(String name, UUID memberId);
    
    // 통계용 메서드
    long countByMemberId(UUID memberId);
}
