package point.ttodoApi.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import point.ttodoApi.challenge.application.ChallengeService;
import point.ttodoApi.category.application.CategoryQueryService;
import point.ttodoApi.todo.application.TodoTemplateService;
import point.ttodoApi.todo.application.CollaborativeTodoService;
import point.ttodoApi.todo.domain.TodoId;

import java.io.Serializable;
import java.util.UUID;

/**
 * 중앙화된 권한 관리 서비스
 * 모든 리소스에 대한 권한 검증 로직을 통합 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final SecurityService securityService;
    private final ChallengeService challengeService;
    private final CategoryQueryService categoryQueryService;
    private final TodoTemplateService todoTemplateService;
    private final CollaborativeTodoService collaborativeTodoService;

    /**
     * 통합 권한 검증 메서드
     * 
     * @param userId 사용자 ID
     * @param targetId 대상 리소스 ID
     * @param targetType 리소스 타입 (Challenge, Category, Todo, User 등)
     * @param permission 권한 타입 (READ, WRITE, ACCESS 등)
     * @return 권한 보유 여부
     */
    public boolean hasPermission(UUID userId, Serializable targetId, String targetType, String permission) {
        try {
            return switch (targetType.toUpperCase()) {
                case "CHALLENGE" -> evaluateChallengePermission(userId, targetId, permission);
                case "CATEGORY" -> evaluateCategoryPermission(userId, targetId, permission);
                case "TODO" -> evaluateTodoPermission(userId, targetId, permission);
                case "USER" -> evaluateUserPermission(userId, targetId, permission);
                case "TAG" -> evaluateTagPermission(userId, permission);
                default -> {
                    log.warn("Unknown target type: {}", targetType);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("Error evaluating permission for {}#{}", targetType, targetId, e);
            return false;
        }
    }

    private boolean evaluateChallengePermission(UUID userId, Serializable targetId, String permission) {
        Long challengeId = convertToLong(targetId);
        if (challengeId == null) return false;

        return switch (permission.toUpperCase()) {
            case "READ" -> true; // 인증된 사용자는 챌린지 읽기 가능
            case "WRITE", "DELETE" -> challengeService.isOwner(challengeId, userId);
            default -> false;
        };
    }

    private boolean evaluateCategoryPermission(UUID userId, Serializable targetId, String permission) {
        UUID categoryId = convertToUUID(targetId);
        if (categoryId == null) return false;

        return switch (permission.toUpperCase()) {
            case "READ", "WRITE", "DELETE" -> categoryQueryService.isUser(categoryId, userId);
            default -> false;
        };
    }

    private boolean evaluateTodoPermission(UUID userId, Serializable targetId, String permission) {
        // TodoId 형태 처리: "todoId:daysDifference" 또는 단순 Long
        if (targetId instanceof String) {
            String[] parts = targetId.toString().split(":");
            if (parts.length == 2) {
                try {
                    Long todoId = Long.parseLong(parts[0]);
                    Long daysDifference = Long.parseLong(parts[1]);
                    return switch (permission.toUpperCase()) {
                        case "READ", "WRITE", "DELETE" -> 
                            todoTemplateService.isOwnerWithDaysDifference(todoId, daysDifference, userId);
                        default -> false;
                    };
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        Long todoId = convertToLong(targetId);
        if (todoId == null) return false;

        return switch (permission.toUpperCase()) {
            case "READ", "WRITE", "DELETE" -> todoTemplateService.isOwner(todoId, userId);
            default -> false;
        };
    }

    private boolean evaluateUserPermission(UUID userId, Serializable targetId, String permission) {
        UUID targetUserId = convertToUUID(targetId);
        if (targetUserId == null) return false;

        return switch (permission.toUpperCase()) {
            case "ACCESS" -> securityService.canAccessUser(targetUserId);
            case "READ" -> securityService.canAccessUser(targetUserId);
            case "WRITE" -> securityService.isSameUser(targetUserId) || securityService.isAdmin();
            default -> false;
        };
    }

    private boolean evaluateTagPermission(UUID userId, String permission) {
        return switch (permission.toUpperCase()) {
            case "READ" -> todoTemplateService.canAccessTags(userId);
            default -> false;
        };
    }

    private Long convertToLong(Serializable id) {
        try {
            if (id instanceof Long) return (Long) id;
            if (id instanceof String) return Long.parseLong((String) id);
            if (id instanceof Number) return ((Number) id).longValue();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private UUID convertToUUID(Serializable id) {
        try {
            if (id instanceof UUID) return (UUID) id;
            if (id instanceof String) return UUID.fromString((String) id);
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}