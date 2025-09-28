package point.ttodoApi.profile.infrastructure.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.profile.domain.Profile;

import java.util.*;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
  @Query("SELECT p FROM Profile p WHERE p.owner.id = :ownerId")
  Optional<Profile> findByOwnerId(@Param("ownerId") UUID ownerId);

  @Query("SELECT COUNT(p) > 0 FROM Profile p WHERE p.owner.id = :ownerId")
  boolean existsByOwnerId(@Param("ownerId") UUID ownerId);

  @Modifying
  @Query("DELETE FROM Profile p WHERE p.owner.id = :ownerId")
  void deleteByOwnerId(@Param("ownerId") UUID ownerId);
}
