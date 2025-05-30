package point.zzicback.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import point.zzicback.common.properties.JwtProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;

    private String generateToken(String userId, Instant expiresAt, Map<String, Object> additionalClaims) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(userId)
                .issuedAt(now)
                .expiresAt(expiresAt);

        additionalClaims.forEach(claimsBuilder::claim);

        JwtClaimsSet claims = claimsBuilder.build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
                JwsHeader.with(() -> "RS256")
                        .keyId(jwtProperties.keyId())
                        .build(),
                claims
        );
        return jwtEncoder.encode(parameters).getTokenValue();
    }

    public String generateAccessToken(String id, String email, String nickname) {
        Instant expiresAt = Instant.now().plus(jwtProperties.expiration(), ChronoUnit.SECONDS);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("nickname", nickname);
        claims.put("scope", "ROLE_USER");
        return generateToken(id, expiresAt, claims);
    }

    public String generateRefreshToken(String id, String device) {
        Instant expiresAt = Instant.now().plus(jwtProperties.refreshExpiration(), ChronoUnit.SECONDS);
        Map<String, Object> claims = new HashMap<>();
        claims.put("device", device);
        return generateToken(id, expiresAt, claims);
    }

    public String extractClaim(String token, String claimName) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            // 매우 단순한 claim 추출 (key는 "..."로 감싸져 있다고 가정)
            String target = "\"" + claimName + "\"";
            int start = payloadJson.indexOf(target);
            if (start == -1) return null;

            int colon = payloadJson.indexOf(':', start);
            int valueStart = payloadJson.indexOf('"', colon + 1) + 1;
            int valueEnd = payloadJson.indexOf('"', valueStart);

            return payloadJson.substring(valueStart, valueEnd);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 파싱 실패", e);
        }
    }


}
