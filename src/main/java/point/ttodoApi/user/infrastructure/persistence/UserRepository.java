package point.ttodoApi.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.user.application.dto.AuthUserProjection;
import point.ttodoApi.user.application.dto.UserNicknameProjection;
import point.ttodoApi.user.application.dto.UserWithProfileProjection;
import point.ttodoApi.user.domain.User;

import java.util.*;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  // ===== OPTIMIZED QUERIES FOR NICKNAME CONSOLIDATION =====
  
  /**
   * Find user with profile data in single query
   * Eliminates N+1 query problem when accessing user and profile data
   * 
   * @param userId User ID
   * @return UserWithProfileProjection containing combined user+profile data
   */
  @Query("""
      SELECT new point.ttodoApi.user.application.dto.UserWithProfileProjection(
          u.id, 
          u.email, 
          p.nickname, 
          p.timeZone, 
          p.locale
      )
      FROM User u 
      JOIN Profile p ON p.owner = u 
      WHERE u.id = :userId
      """)
  Optional<UserWithProfileProjection> findUserWithProfile(@Param("userId") UUID userId);
  
  /**
   * Find multiple users with profile data in single query
   * Bulk operation for performance optimization
   * 
   * @param userIds Collection of user IDs
   * @return List of UserWithProfileProjection
   */
  @Query("""
      SELECT new point.ttodoApi.user.application.dto.UserWithProfileProjection(
          u.id, 
          u.email, 
          p.nickname, 
          p.timeZone, 
          p.locale
      )
      FROM User u 
      JOIN Profile p ON p.owner = u 
      WHERE u.id IN :userIds
      """)
  List<UserWithProfileProjection> findUsersWithProfile(@Param("userIds") Collection<UUID> userIds);
  
  /**
   * Find nicknames for multiple users
   * Optimized for bulk nickname retrieval
   * 
   * @param userIds Collection of user IDs
   * @return List of user ID and nickname pairs
   */
  @Query("""
      SELECT new point.ttodoApi.user.application.dto.UserNicknameProjection(
          u.id, 
          p.nickname
      )
      FROM User u 
      JOIN Profile p ON p.owner = u 
      WHERE u.id IN :userIds
      """)
  List<UserNicknameProjection> findNicknamesByUserIds(@Param("userIds") Collection<UUID> userIds);
  
  /**
   * Find user for authentication purposes
   * Returns essential authentication data in single query
   * 
   * @param email User email
   * @return AuthUserProjection with authentication-relevant data
   */
  @Query("""
      SELECT new point.ttodoApi.user.application.dto.AuthUserProjection(
          u.id,
          u.email,
          u.password,
          p.nickname,
          p.timeZone,
          p.locale
      )
      FROM User u 
      JOIN Profile p ON p.owner = u 
      WHERE u.email = :email
      """)
  Optional<AuthUserProjection> findByEmailWithProfile(@Param("email") String email);
  
  /**
   * Find users by nickname search (from Profile)
   * Search functionality using Profile.nickname
   * 
   * @param nicknamePattern Nickname search pattern
   * @return List of UserWithProfileProjection matching search
   */
  @Query("""
      SELECT new point.ttodoApi.user.application.dto.UserWithProfileProjection(
          u.id, 
          u.email, 
          p.nickname, 
          p.timeZone, 
          p.locale
      )
      FROM User u 
      JOIN Profile p ON p.owner = u 
      WHERE p.nickname LIKE :nicknamePattern
      ORDER BY p.nickname
      """)
  List<UserWithProfileProjection> findByNicknameContaining(@Param("nicknamePattern") String nicknamePattern);
  
}
