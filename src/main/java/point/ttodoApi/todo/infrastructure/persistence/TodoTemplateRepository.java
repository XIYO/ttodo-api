package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.todo.domain.TodoTemplate;

import java.util.*;

public interface TodoTemplateRepository extends JpaRepository<TodoTemplate, Long> {

  @Query("SELECT t FROM TodoTemplate t WHERE t.owner.id = :userId AND t.active = true")
  List<TodoTemplate> findByOwnerId(@Param("userId") UUID userId);

  @Query("SELECT t FROM TodoTemplate t WHERE t.id = :todoTemplateId AND t.owner.id = :userId AND t.active = true")
  Optional<TodoTemplate> findByIdAndOwnerId(@Param("todoTemplateId") Long todoTemplateId, @Param("userId") UUID userId);

  @Query("SELECT DISTINCT tag FROM TodoTemplate t JOIN t.tags tag WHERE t.owner.id = :userId " +
          "AND t.active = true " +
          "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
  Page<String> findDistinctTagsByOwnerId(@Param("userId") UUID userId,
                                         @Param("categoryIds") List<UUID> categoryIds,
                                         Pageable pageable);

  @Query("SELECT t FROM TodoTemplate t WHERE t.owner.id = :userId AND t.isPinned = true AND t.active = true ORDER BY t.displayOrder ASC")
  List<TodoTemplate> findByOwnerIdAndIsPinnedTrueOrderByDisplayOrderAsc(@Param("userId") UUID userId);
}
