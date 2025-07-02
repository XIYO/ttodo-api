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
        AND (:complete IS NULL OR t.complete = :complete)
        AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)
        AND (:priorityIds IS NULL OR t.priorityId IN :priorityIds)
        AND (:tags IS NULL OR tag IN :tags)
        AND (:keyword IS NULL OR :keyword = '' OR
             LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(tag) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:startDate IS NULL OR t.date IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date IS NULL OR t.date <= :endDate)
        ORDER BY
          CASE WHEN t.date IS NULL AND t.time IS NULL THEN 1 ELSE 0 END,
          CASE WHEN t.complete = false THEN 0 ELSE 1 END,
          t.date ASC,
          t.time ASC,
          t.createdAt ASC
        """)
    Page<TodoOriginal> findByFilters(@Param("memberId") UUID memberId,
                                   @Param("complete") Boolean complete,
                                   @Param("categoryIds") List<Long> categoryIds,
                                   @Param("priorityIds") List<Integer> priorityIds,
                                   @Param("tags") List<String> tags,
                                   @Param("keyword") String keyword,
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
