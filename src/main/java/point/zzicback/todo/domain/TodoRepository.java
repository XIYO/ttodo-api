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
    Page<Todo> findByMemberIdAndStatus(UUID memberId, TodoStatus status, Pageable pageable);
    Page<Todo> findByMemberId(UUID memberId, Pageable pageable);
    Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
    long countByMemberId(UUID memberId);

    Optional<Todo> findByMemberIdAndTitle(UUID memberId, String title);

    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId AND t.status = 'IN_PROGRESS' AND t.dueDate < :currentDate")
    Page<Todo> findOverdueTodos(@Param("memberId") UUID memberId, @Param("currentDate") LocalDate currentDate, Pageable pageable);
    
    @Query("""
        SELECT t FROM Todo t WHERE t.member.id = :memberId
        AND (:status IS NULL OR t.status = :status)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (:priority IS NULL OR t.priority = :priority)
        AND (:keyword IS NULL OR :keyword = '' OR 
             LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Todo> findByFilters(@Param("memberId") UUID memberId, 
                           @Param("status") TodoStatus status,
                           @Param("categoryId") Long categoryId,
                           @Param("priority") Integer priority,
                           @Param("keyword") String keyword,
                           Pageable pageable);
}

