package point.zzicback.common.util;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.zzicback.common.properties.JwtProperties;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties jwtProperties;

    public Cookie createJwtCookie(String jwtToken) {
        String cookieName = jwtProperties.cookie().name();
        int maxAge = jwtProperties.expiration();
        return this.create(cookieName, jwtToken, maxAge);
    }

    public Cookie create(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(jwtProperties.cookie().path());
        cookie.setSecure(jwtProperties.cookie().secure());
        cookie.setHttpOnly(jwtProperties.cookie().httpOnly());

        // 개발 환경(localhost)에서는 domain 설정을 하지 않음
        String domain = jwtProperties.cookie().domain();
        if (domain != null && !domain.equalsIgnoreCase("localhost") && !domain.isBlank()) {
            cookie.setDomain(domain);
        }

        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public void zeroAge(Cookie cookie) {
        cookie.setMaxAge(0);
    }

    public Cookie createRefreshCookie(String refreshToken) {
        String cookieName = jwtProperties.cookie().name() + "-refresh";
        int maxAge = jwtProperties.refreshExpiration();
        return this.create(cookieName, refreshToken, maxAge);
    }

    public String getRefreshToken(jakarta.servlet.http.HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(jwtProperties.cookie().name() + "-refresh")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
