package point.zzicback.todo.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.todo.domain.Todo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByDone(Boolean done);

    List<Todo> findByMemberIdAndDone(UUID memberId, Boolean done);

    Optional<Todo> findByIdAndMember_Id(Long todoId, UUID memberId);
}


