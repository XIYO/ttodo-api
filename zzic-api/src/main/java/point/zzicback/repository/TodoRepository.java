package point.zzicback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
}
