package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;

import java.time.LocalDate;
import java.util.*;

/**
 * 완료된 Todo(가상 투두가 완료되어 실제로 저장된 것)를 위한 Repository 인터페이스
 */
public interface TodoRepository extends JpaRepository<Todo, TodoId>, JpaSpecificationExecutor<Todo> {
    
    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.member.id = :memberId AND t.active = true")
    Optional<Todo> findByTodoIdAndMemberId(@Param("todoId") TodoId todoId, @Param("memberId") UUID memberId);

    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.member.id = :memberId")
    Optional<Todo> findByTodoIdAndMemberIdIgnoreActive(@Param("todoId") TodoId todoId, @Param("memberId") UUID memberId);
    
    // 통계용 메서드 - 완료한 할일 수
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.member.id = :memberId AND t.complete = true AND t.active = true")
    long countCompletedTodosByMemberId(@Param("memberId") UUID memberId);

}
