package point.ttodoApi.experience.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.ttodoApi.experience.domain.MemberExperience;

import java.util.*;

public interface MemberExperienceRepository extends JpaRepository<MemberExperience, Long> {
    Optional<MemberExperience> findByOwnerId(UUID ownerId);
}
