package point.ttodoApi.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.UUID;

/**
 * Spring Security 표준 PermissionEvaluator 구현
 * hasPermission() 표현식에서 사용되는 중앙화된 권한 평가 로직
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AuthorizationService authorizationService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        try {
            UUID userId = extractUserId(authentication);
            if (userId == null) {
                return false;
            }

            String permissionStr = permission.toString();
            log.debug("Evaluating permission: {} for user: {} on object: {}", 
                     permissionStr, userId, targetDomainObject.getClass().getSimpleName());

            // 여기서는 targetDomainObject를 사용하는 경우는 현재 없으므로 기본 구현만 제공
            return false;
        } catch (Exception e) {
            log.error("Error evaluating permission", e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null || permission == null) {
            return false;
        }

        try {
            UUID userId = extractUserId(authentication);
            if (userId == null) {
                return false;
            }

            String permissionStr = permission.toString();
            log.debug("Evaluating permission: {} for user: {} on {}#{}", 
                     permissionStr, userId, targetType, targetId);

            return authorizationService.hasPermission(userId, targetId, targetType, permissionStr);
        } catch (Exception e) {
            log.error("Error evaluating permission for {}#{}", targetType, targetId, e);
            return false;
        }
    }

    private UUID extractUserId(Authentication authentication) {
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User user = 
                    (org.springframework.security.core.userdetails.User) principal;
                return UUID.fromString(user.getUsername());
            }
            if (principal instanceof String) {
                return UUID.fromString((String) principal);
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract user ID from authentication", e);
            return null;
        }
    }
}