package point.ttodoApi.level.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.ttodoApi.level.domain.Level;

public interface LevelRepository extends JpaRepository<Level, Integer> {
}
