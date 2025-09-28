package point.ttodoApi.experience.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import point.ttodoApi.experience.domain.UserExperience;

import java.util.*;

public interface UserExperienceRepository extends JpaRepository<UserExperience, Long> {
  Optional<UserExperience> findByOwnerId(UUID ownerId);
}
