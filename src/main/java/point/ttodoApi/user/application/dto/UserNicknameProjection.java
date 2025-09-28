package point.ttodoApi.user.application.dto;

import java.util.UUID;

/**
 * Projection for User ID and Nickname only
 * Used for bulk nickname retrieval optimization
 */
public record UserNicknameProjection(
    UUID userId, 
    String nickname
) {}