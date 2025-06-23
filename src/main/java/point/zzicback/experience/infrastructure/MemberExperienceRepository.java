package point.zzicback.experience.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.experience.domain.MemberExperience;

import java.util.*;

public interface MemberExperienceRepository extends JpaRepository<MemberExperience, Long> {
    Optional<MemberExperience> findByMemberId(UUID memberId);
}
