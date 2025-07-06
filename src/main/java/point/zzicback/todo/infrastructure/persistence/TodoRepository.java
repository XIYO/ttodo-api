package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;

import java.time.LocalDate;
import java.util.*;

/**
 * 완료된 Todo(가상 투두가 완료되어 실제로 저장된 것)를 위한 Repository 인터페이스
 */
public interface TodoRepository extends JpaRepository<Todo, TodoId> {
    @Query("""
        SELECT t FROM Todo t WHERE t.member.id = :memberId
        AND t.active = true
        AND (:complete IS NULL OR t.complete = :complete)
        AND (:#{#categoryIds == null || #categoryIds.isEmpty()} = true OR t.category.id IN :categoryIds)
        AND (:#{#priorityIds == null || #priorityIds.isEmpty()} = true OR t.priorityId IN :priorityIds)
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

    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.member.id = :memberId AND t.active = true")
    Optional<Todo> findByTodoIdAndMemberId(@Param("todoId") TodoId todoId, @Param("memberId") UUID memberId);

    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.member.id = :memberId")
    Optional<Todo> findByTodoIdAndMemberIdIgnoreActive(@Param("todoId") TodoId todoId, @Param("memberId") UUID memberId);

}
