package point.zzicback.common.utill;

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
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;

    private String generateJwtToken(String id, String email, String nickname, Instant expiresAt) {
        Instant now = Instant.now();

        List<String> roles = Collections.singletonList("ROLE_USER");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(id)
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("scope", String.join(" ", roles))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

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
        return generateJwtToken(id, email, nickname, expiresAt);
    }

    public String generateRefreshToken(String id, String email, String nickname) {
        Instant expiresAt = Instant.now().plus(365, ChronoUnit.DAYS);
        return generateJwtToken(id, email, nickname, expiresAt);
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