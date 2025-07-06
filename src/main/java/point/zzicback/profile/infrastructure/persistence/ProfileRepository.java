package point.zzicback.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.profile.domain.Profile;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
}