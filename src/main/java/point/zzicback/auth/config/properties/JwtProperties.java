package point.zzicback.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String keyId, Resource publicKey, Resource privateKey, AccessTokenProperties accessToken,
                            RefreshTokenProperties refreshToken) {
  public record AccessTokenProperties(CookieProperties cookie, int expiration) {
  }

  public record RefreshTokenProperties(CookieProperties cookie, int expiration) {
  }

  public record CookieProperties(String name, String domain, String path, boolean secure, boolean httpOnly,
                                 String sameSite) {
  }
}
