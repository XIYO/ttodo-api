package point.zzicback.todo.persistance;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.todo.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    Page<Todo> findByMemberIdAndDone(UUID memberId, Boolean done, Pageable pageable);

    Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
}
