package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.Todo;

import java.time.LocalDate;
import java.time.LocalTime;
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
          t.dueTime ASC,
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
        AND (:statuses IS NULL OR t.statusId IN :statuses)
        AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)
        AND (:priorityIds IS NULL OR t.priorityId IN :priorityIds)
        AND (:tags IS NULL OR tag IN :tags)
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
          t.dueTime ASC,
          t.createdAt ASC
        """)
    Page<Todo> findByFilters(@Param("memberId") UUID memberId,
                           @Param("statuses") List<Integer> statuses,
                           @Param("categoryIds") List<Long> categoryIds,
                           @Param("priorityIds") List<Integer> priorityIds,
                           @Param("tags") List<String> tags,
                           @Param("keyword") String keyword,
                           @Param("hideStatusIds") List<Integer> hideStatusIds,
                           @Param("startDate") LocalDate startDate,
                           @Param("endDate") LocalDate endDate,
                           Pageable pageable);

    @Modifying
    @Query("UPDATE Todo t SET t.statusId = 2 WHERE t.statusId = 0 AND (t.dueDate < :currentDate OR (t.dueDate = :currentDate AND t.dueTime IS NOT NULL AND t.dueTime < :currentTime))")
    int updateOverdueTodos(@Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId")
    long countByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.statusId = 0")
    long countInProgressByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.statusId = 1")
    long countCompletedByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND (t.statusId = 2 OR (t.statusId = 0 AND (t.dueDate < :currentDate OR (t.dueDate = :currentDate AND t.dueTime IS NOT NULL AND t.dueTime < :currentTime))))")
    long countOverdueByMemberId(@Param("memberId") UUID memberId, @Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime);
    
    @Query("SELECT DISTINCT tag FROM Todo t JOIN t.tags tag WHERE t.member.id = :memberId " +
           "AND (:categoryIds IS NULL OR t.category.id IN :categoryIds)")
    Page<String> findDistinctTagsByMemberId(@Param("memberId") UUID memberId,
                                            @Param("categoryIds") List<Long> categoryIds,
                                            Pageable pageable);
}
