package point.zzicback.todo.persistance;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.todo.domain.Todo;

import java.util.*;

public interface TodoRepository extends JpaRepository<Todo, Long> {
  Page<Todo> findByMemberIdAndDone(UUID memberId, Boolean done, Pageable pageable);

  Optional<Todo> findByIdAndMemberId(Long todoId, UUID memberId);
}
