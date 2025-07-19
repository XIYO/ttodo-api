package point.ttodoApi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.profile.domain.Profile;

import java.util.*;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("SELECT p FROM Profile p WHERE p.ownerId = :ownerId")
    Optional<Profile> findByOwnerId(@Param("ownerId") UUID ownerId);
    @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.ownerId = :ownerId")
    boolean existsByOwnerId(@Param("ownerId") UUID ownerId);
}