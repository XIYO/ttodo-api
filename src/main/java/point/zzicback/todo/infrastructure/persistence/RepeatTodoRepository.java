package point.zzicback.todo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.todo.domain.RepeatTodo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepeatTodoRepository extends JpaRepository<RepeatTodo, Long> {
    
    List<RepeatTodo> findByMemberIdAndIsActiveTrue(UUID memberId);
    
    Optional<RepeatTodo> findByTodoId(Long todoId);
}
