package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * 완료된 Todo(가상 투두가 완료되어 실제로 저장된 것)를 위한 Repository 인터페이스
 */
public interface TodoRepository extends JpaRepository<Todo, TodoId> {
    @Query("""
        SELECT t FROM Todo t WHERE t.member.id = :memberId
        AND (:complete IS NULL OR t.complete = :complete)
        AND (:startDate IS NULL OR t.date IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date IS NULL OR t.date <= :endDate)
        ORDER BY
          CASE WHEN t.date IS NULL AND t.time IS NULL THEN 1 ELSE 0 END,
          CASE WHEN t.complete = false THEN 0 ELSE 1 END,
          t.date ASC,
          t.time ASC,
          t.createdAt ASC
        """)
    Page<Todo> findByMemberId(@Param("memberId") UUID memberId,
                             @Param("categoryIds") List<Long> categoryIds,
                             @Param("complete") Boolean complete,
                             @Param("priorityIds") List<Integer> priorityIds,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             Pageable pageable);

    Optional<Todo> findByTodoIdAndMemberId(TodoId todoId, UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId")
    long countByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.complete = false")
    long countInProgressByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.complete = true")
    long countCompletedByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId")
    List<Todo> findAllByMemberId(@Param("memberId") UUID memberId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Todo t WHERE t.member.id = :memberId AND t.date = :dueDate AND t.todoId.id = :originalTodoId")
    boolean existsByMemberIdAndDueDateAndOriginalTodoId(@Param("memberId") UUID memberId, @Param("dueDate") LocalDate dueDate, @Param("originalTodoId") Long originalTodoId);
    
    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId AND t.date = :dueDate AND t.todoId.id = :originalTodoId")
    Optional<Todo> findByMemberIdAndDueDateAndTodoIdId(@Param("memberId") UUID memberId, @Param("dueDate") LocalDate dueDate, @Param("originalTodoId") Long originalTodoId);
    
    void deleteAllByMemberId(UUID memberId);
}
