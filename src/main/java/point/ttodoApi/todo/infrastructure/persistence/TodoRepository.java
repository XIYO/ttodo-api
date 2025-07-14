package point.ttodoApi.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.todo.domain.*;

import java.util.*;

/**
 * 완료된 Todo(가상 투두가 완료되어 실제로 저장된 것)를 위한 Repository 인터페이스
 */
public interface TodoRepository extends JpaRepository<Todo, TodoId>, JpaSpecificationExecutor<Todo> {
    
    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.owner.id = :ownerId AND t.active = true")
    Optional<Todo> findByTodoIdAndOwnerId(@Param("todoId") TodoId todoId, @Param("ownerId") UUID ownerId);

    @Query("SELECT t FROM Todo t WHERE t.todoId = :todoId AND t.owner.id = :ownerId")
    Optional<Todo> findByTodoIdAndOwnerIdIgnoreActive(@Param("todoId") TodoId todoId, @Param("ownerId") UUID ownerId);
    
    // 통계용 메서드 - 완료한 할일 수
    @Query("SELECT COUNT(t) FROM Todo t WHERE t.owner.id = :ownerId AND t.complete = true AND t.active = true")
    long countCompletedTodosByOwnerId(@Param("ownerId") UUID ownerId);

}
