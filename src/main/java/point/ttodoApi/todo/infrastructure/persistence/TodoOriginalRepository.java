package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.todo.domain.TodoOriginal;

import java.util.*;

public interface TodoOriginalRepository extends JpaRepository<TodoOriginal, Long> {
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.owner.id = :ownerId AND t.active = true")
    List<TodoOriginal> findByOwnerId(@Param("ownerId") UUID ownerId);
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.id = :todoOriginalId AND t.owner.id = :ownerId AND t.active = true")
    Optional<TodoOriginal> findByIdAndOwnerId(@Param("todoOriginalId") Long todoOriginalId, @Param("ownerId") UUID ownerId);
    
    @Query("SELECT DISTINCT tag FROM TodoOriginal t JOIN t.tags tag WHERE t.owner.id = :ownerId " +
           "AND t.active = true " +
           "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
    Page<String> findDistinctTagsByOwnerId(@Param("ownerId") UUID ownerId,
                                            @Param("categoryIds") List<UUID> categoryIds,
                                            Pageable pageable);
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.owner.id = :ownerId AND t.isPinned = true AND t.active = true ORDER BY t.displayOrder ASC")
    List<TodoOriginal> findByOwnerIdAndIsPinnedTrueOrderByDisplayOrderAsc(@Param("ownerId") UUID ownerId);
}
