package point.ttodoApi.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.util.*;

import org.springframework.security.core.userdetails.User;

/**
 * Spring Security 컨텍스트에서 현재 사용자 정보를 제공하는 서비스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

  private final UserRepository UserRepository;

  /**
   * 현재 로그인한 멤버 ID 조회
   *
   * @return 로그인한 멤버 ID (Optional)
   */
  public Optional<UUID> getCurrentuserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
      return Optional.empty();
    }

    try {
      // Principal이 User 타입인 경우
      if (authentication.getPrincipal() instanceof User) {
        User user = (User) authentication.getPrincipal();
        return Optional.of(UUID.fromString(user.getUsername()));
      }

      // Principal이 String 타입인 경우 (테스트나 특수한 경우)
      if (authentication.getPrincipal() instanceof String) {
        String principalStr = (String) authentication.getPrincipal();
        if (!"anonymousUser".equals(principalStr)) {
          try {
            UUID userId = UUID.fromString(principalStr);
            return Optional.of(userId);
          } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format in principal: {}", principalStr);
          }
        }
      }

      log.debug("Unsupported principal type: {}", authentication.getPrincipal().getClass());
      return Optional.empty();

    } catch (Exception e) {
      log.error("Error getting current user ID", e);
      return Optional.empty();
    }
  }

  /**
   * 현재 로그인한 멤버 엔티티 조회
   *
   * @return 로그인한 멤버 엔티티 (Optional)
   */
  public Optional<point.ttodoApi.user.domain.User> getCurrentUser() {
    return getCurrentuserId()
            .flatMap(UserRepository::findById);
  }

  /**
   * 현재 사용자가 인증되었는지 확인
   *
   * @return 인증 여부
   */
  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    return authentication != null &&
            authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken);
  }

  /**
   * 현재 사용자가 특정 권한을 가지고 있는지 확인
   *
   * @param role 확인할 권한 (예: "ROLE_USER", "ROLE_ADMIN")
   * @return 권한 보유 여부
   */
  public boolean hasRole(String role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(role));
  }

  /**
   * 현재 사용자가 ADMIN 권한을 가지고 있는지 확인
   *
   * @return ADMIN 권한 보유 여부
   */
  public boolean isAdmin() {
    return hasRole("ROLE_ADMIN");
  }

  /**
   * 현재 사용자가 특정 멤버와 동일한지 확인
   *
   * @param userId 비교할 멤버 ID
   * @return 동일 여부
   */
  public boolean isSameUser(UUID userId) {
    if (userId == null) return false;

    return getCurrentuserId()
            .map(currentId -> currentId.equals(userId))
            .orElse(false);
  }

  /**
   * 현재 사용자가 특정 멤버에 대한 접근 권한이 있는지 확인 (본인이거나 ADMIN)
   *
   * @param userId 접근하려는 멤버 ID
   * @return 접근 권한 보유 여부
   */
  public boolean canAccessUser(UUID userId) {
    return isSameUser(userId) || isAdmin();
  }
}
