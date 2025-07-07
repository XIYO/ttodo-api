package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 완료된 Todo(가상 투두가 완료되어 실제로 저장된 것)를 위한 Repository 인터페이스
 */
public interface TodoRepository extends JpaRepository<Todo, TodoId>, JpaSpecificationExecutor<Todo> {
    
    @EntityGraph(attributePaths = {"category", "member", "tags"})
    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.member.id = :memberId AND t.active = true")
    Optional<Todo> findByTodoIdAndMemberId(@Param("todoId") TodoId todoId, @Param("memberId") UUID memberId);

    @EntityGraph(attributePaths = {"category", "member", "tags"})
    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.member.id = :memberId")
    Optional<Todo> findByTodoIdAndMemberIdIgnoreActive(@Param("todoId") TodoId todoId, @Param("memberId") UUID memberId);
    
    @EntityGraph(attributePaths = {"category", "member", "tags"})
    Page<Todo> findAll(Specification<Todo> spec, Pageable pageable);
    
    @EntityGraph(attributePaths = {"category", "member", "tags"})
    @Query("SELECT t FROM Todo t WHERE t.member.id = :memberId AND t.todoId IN :todoIds")
    List<Todo> findByMemberIdAndTodoIdIn(@Param("memberId") UUID memberId, @Param("todoIds") List<TodoId> todoIds);

}
