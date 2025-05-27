package point.zzicback.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String keyId,
        Resource publicKey,
        Resource privateKey,
        CookieProperties cookie,
        int expiration
) {
    public record CookieProperties(
            String name,
            String domain,
            String path,
            boolean secure,
            boolean httpOnly,
            String sameSite,
            int maxAge
    ) {}
}