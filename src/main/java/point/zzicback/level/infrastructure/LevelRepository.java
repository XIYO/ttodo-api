package point.zzicback.level.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.level.domain.Level;

public interface LevelRepository extends JpaRepository<Level, Integer> {
}
