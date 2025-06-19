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
    Page<Todo> findByMemberId(UUID memberId, Pageable pageable);
    Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
    
    @Query("""
        SELECT t FROM Todo t WHERE t.member.id = :memberId
        AND (:status IS NULL OR t.statusId = :status)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:priority IS NULL OR t.priorityId = :priority)
        AND (:keyword IS NULL OR :keyword = '' OR 
             LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Todo> findByFilters(@Param("memberId") UUID memberId, 
                           @Param("status") Integer status,
                           @Param("categoryId") Long categoryId,
                           @Param("priority") Integer priority,
                           @Param("keyword") String keyword,
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

