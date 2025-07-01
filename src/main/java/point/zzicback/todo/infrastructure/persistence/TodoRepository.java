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
    Page<Todo> findByMemberId(@Param("memberId") UUID memberId,
                             @Param("categoryIds") List<Long> categoryIds,
                             @Param("statusIds") List<Integer> statusIds,
                             @Param("priorityIds") List<Integer> priorityIds,
                             @Param("hideStatusIds") List<Integer> hideStatusIds,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             Pageable pageable);

    Optional<Todo> findByTodoIdAndMemberId(TodoId todoId, UUID memberId);
    
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
    
    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId")
    List<Todo> findAllByMemberId(@Param("memberId") UUID memberId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Todo t WHERE t.member.id = :memberId AND t.dueDate = :dueDate AND t.todoId.id = :originalTodoId")
    boolean existsByMemberIdAndDueDateAndOriginalTodoId(@Param("memberId") UUID memberId, @Param("dueDate") LocalDate dueDate, @Param("originalTodoId") Long originalTodoId);
    
    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId AND t.dueDate = :dueDate AND t.todoId.id = :originalTodoId")
    Optional<Todo> findByMemberIdAndDueDateAndTodoIdId(@Param("memberId") UUID memberId, @Param("dueDate") LocalDate dueDate, @Param("originalTodoId") Long originalTodoId);
    
    void deleteAllByMemberId(UUID memberId);
}
