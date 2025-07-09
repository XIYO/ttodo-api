package point.ttodoApi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import point.ttodoApi.profile.domain.Profile;

import java.util.*;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
}