package point.zzicback.anonymousTodo.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.anonymousTodo.domain.AnonymousTodo;

import java.util.List;
import java.util.Optional;

public interface AnonymousTodoRepository extends JpaRepository<AnonymousTodo, Long> {
    List<AnonymousTodo> findAllByGuestId(String guestId);
    List<AnonymousTodo> findAllByGuestIdAndDone(String guestId, Boolean done);

    Optional<AnonymousTodo> findByIdAndGuestId(Long id, String guestId);
}
