package point.ttodoApi.category.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import point.ttodoApi.category.domain.Category;

import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    List<Category> findByOwnerIdOrderByNameAsc(UUID ownerId);
    Page<Category> findByOwnerId(UUID ownerId, Pageable pageable);
    Optional<Category> findByIdAndOwnerId(UUID id, UUID ownerId);
    boolean existsByNameAndOwnerId(String name, UUID ownerId);
    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);
    Optional<Category> findByNameAndOwnerId(String name, UUID ownerId);
    
    // 통계용 메서드
    long countByOwnerId(UUID ownerId);
}
