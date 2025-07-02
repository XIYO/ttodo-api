package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.TodoOriginal;

import java.time.LocalDate;
import java.util.*;

public interface TodoOriginalRepository extends JpaRepository<TodoOriginal, Long> {
    
    @Query("""
        SELECT DISTINCT t FROM TodoOriginal t LEFT JOIN t.tags tag WHERE t.member.id = :memberId
        AND (:statusIds IS NULL OR t.statusId IN :statusIds)
        AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)
        AND (:priorityIds IS NULL OR t.priorityId IN :priorityIds)
        AND (:tags IS NULL OR tag IN :tags)
        AND (:keyword IS NULL OR :keyword = '' OR
             LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(tag) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:hideStatusIds IS NULL OR t.statusId NOT IN :hideStatusIds)
        AND (:startDate IS NULL OR t.dueDate IS NULL OR t.dueDate >= :startDate)
        AND (:endDate IS NULL OR t.dueDate IS NULL OR t.dueDate <= :endDate)
        ORDER BY
          CASE WHEN t.dueDate IS NULL AND t.dueTime IS NULL THEN 1 ELSE 0 END,
          CASE t.statusId
            WHEN 2 THEN 0
            WHEN 0 THEN 1
            WHEN 1 THEN 2
            ELSE 3 END,
          t.dueDate ASC,
          t.dueTime ASC,
          t.createdAt ASC
        """)
    Page<TodoOriginal> findByFilters(@Param("memberId") UUID memberId,
                                   @Param("statusIds") List<Integer> statusIds,
                                   @Param("categoryIds") List<Long> categoryIds,
                                   @Param("priorityIds") List<Integer> priorityIds,
                                   @Param("tags") List<String> tags,
                                   @Param("keyword") String keyword,
                                   @Param("hideStatusIds") List<Integer> hideStatusIds,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   Pageable pageable);

    List<TodoOriginal> findByMemberId(UUID memberId);
    
    Optional<TodoOriginal> findByIdAndMemberId(Long todoOriginalId, UUID memberId);
    
    @Query("SELECT DISTINCT tag FROM TodoOriginal t JOIN t.tags tag WHERE t.member.id = :memberId " +
           "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
    Page<String> findDistinctTagsByMemberId(@Param("memberId") UUID memberId,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            Pageable pageable);
    
    @Query("SELECT t FROM TodoOriginal t WHERE t.member.id = :memberId")
    List<TodoOriginal> findAllTodoOriginalsByMemberId(@Param("memberId") UUID memberId);
    
    void deleteAllByMemberId(UUID memberId);
}
