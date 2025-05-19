package point.zzicback.todo.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.todo.domain.Todo;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByDone(Boolean done);
}
