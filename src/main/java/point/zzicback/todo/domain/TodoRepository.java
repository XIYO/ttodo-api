package point.zzicback.todo.domain;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.*;

/**
 * Todo 도메인을 위한 Repository 인터페이스
 * DDD 원칙에 따라 도메인 계층에 위치
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
    @Query("""
        SELECT t FROM Todo t WHERE t.member.id = :memberId
        AND (:hideStatusIds IS NULL OR t.statusId NOT IN :hideStatusIds)
        AND (:startDate IS NULL OR t.dueDate >= :startDate)
        AND (:endDate IS NULL OR t.dueDate <= :endDate)
        ORDER BY
          CASE t.statusId
            WHEN 2 THEN 0
            WHEN 0 THEN 1
            WHEN 1 THEN 2
            ELSE 3 END,
          t.dueDate ASC,
          t.createdAt ASC
        """)
    Page<Todo> findByMemberId(@Param("memberId") UUID memberId,
                             @Param("hideStatusIds") List<Integer> hideStatusIds,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             Pageable pageable);

    Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
    
    @Query("""
        SELECT DISTINCT t FROM Todo t LEFT JOIN t.tags tag WHERE t.member.id = :memberId
        AND (:status IS NULL OR t.statusId = :status)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:priorityId IS NULL OR t.priorityId = :priorityId)
        AND (:keyword IS NULL OR :keyword = '' OR 
             LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(tag) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:hideStatusIds IS NULL OR t.statusId NOT IN :hideStatusIds)
        AND (:startDate IS NULL OR t.dueDate >= :startDate)
        AND (:endDate IS NULL OR t.dueDate <= :endDate)
        ORDER BY
          CASE t.statusId
            WHEN 2 THEN 0
            WHEN 0 THEN 1
            WHEN 1 THEN 2
            ELSE 3 END,
          t.dueDate ASC,
          t.createdAt ASC
        """)
    Page<Todo> findByFilters(@Param("memberId") UUID memberId, 
                           @Param("status") Integer status,
                           @Param("categoryId") Long categoryId,
                           @Param("priorityId") Integer priorityId,
                           @Param("keyword") String keyword,
                           @Param("hideStatusIds") List<Integer> hideStatusIds,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           Pageable pageable);

    @Modifying
    @Query("UPDATE Todo t SET t.statusId = 2 WHERE t.statusId = 0 AND t.dueDate < :currentDate")
    int updateOverdueTodos(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId")
    long countByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.statusId = 0")
    long countInProgressByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.statusId = 1")
    long countCompletedByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND (t.statusId = 2 OR (t.statusId = 0 AND t.dueDate < :currentDate))")
    long countOverdueByMemberId(@Param("memberId") UUID memberId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT DISTINCT tag FROM Todo t JOIN t.tags tag WHERE t.member.id = :memberId")
    Page<String> findDistinctTagsByMemberId(@Param("memberId") UUID memberId, Pageable pageable);
}
