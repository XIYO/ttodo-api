package point.zzicback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.model.Todo;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByDone(Boolean done);
}
