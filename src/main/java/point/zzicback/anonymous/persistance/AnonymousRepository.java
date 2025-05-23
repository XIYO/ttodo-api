package point.zzicback.anonymous.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.anonymous.domain.Anonymous;

import java.util.Optional;
import java.util.UUID;

public interface AnonymousRepository  extends JpaRepository<Anonymous, UUID> {
    Optional<Anonymous> findByEmail(String email);
}
