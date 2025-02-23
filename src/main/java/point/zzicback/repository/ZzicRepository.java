package point.zzicback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.domain.Zzic;

public interface ZzicRepository extends JpaRepository<Zzic, Long> {
}
