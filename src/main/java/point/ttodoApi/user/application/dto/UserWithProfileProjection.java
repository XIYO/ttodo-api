package point.ttodoApi.user.application.dto;

import java.util.UUID;

/**
 * Projection for User with Profile data
 * 
 * This projection eliminates N+1 queries by fetching user and profile data in a single query.
 * Profile.nickname is used as the single source of truth for display names.
 */
public record UserWithProfileProjection(
    UUID id,
    String email,
    String nickname,    // From Profile table (single source of truth)
    String timeZone,    // From Profile table
    String locale       // From Profile table
) {
    
    /**
     * Convert to legacy format for backward compatibility
     */
    public LegacyUserResponse toLegacyUserResponse() {
        return new LegacyUserResponse(id, email, nickname);
    }
    
    /**
     * Convert to authentication format
     */
    public AuthUserInfo toAuthUserInfo() {
        return new AuthUserInfo(id, email, nickname, timeZone, locale);
    }
    
    // Legacy support classes
    public record LegacyUserResponse(UUID id, String email, String nickname) {}
    public record AuthUserInfo(UUID id, String email, String nickname, String timeZone, String locale) {}
}