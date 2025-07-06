package point.zzicback.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.profile.domain.MemberProfile;

import java.util.Optional;
import java.util.UUID;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
    Optional<MemberProfile> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
}