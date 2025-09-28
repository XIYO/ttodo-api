package point.ttodoApi.user.application.dto;

import java.util.UUID;

/**
 * Projection for Authentication purposes
 * Contains all data needed for JWT token generation
 */
public record AuthUserProjection(
    UUID id,
    String email, 
    String password,
    String nickname,    // From Profile table (single source of truth)
    String timeZone,    // From Profile table
    String locale       // From Profile table
) {}