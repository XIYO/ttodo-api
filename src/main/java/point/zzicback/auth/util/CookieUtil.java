package point.zzicback.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import point.zzicback.auth.config.properties.JwtProperties;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final JwtProperties jwtProperties;

    private String getRefreshCookieName() {
        return jwtProperties.cookie().name() + "-refresh";
    }

    public Cookie createJwtCookie(String jwtToken) {
        return create(jwtProperties.cookie().name(), jwtToken, jwtProperties.expiration());
    }

    public Cookie createRefreshCookie(String refreshToken) {
        return create(getRefreshCookieName(), refreshToken, jwtProperties.refreshExpiration());
    }

    public Cookie create(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(jwtProperties.cookie().path());
        cookie.setSecure(jwtProperties.cookie().secure());
        cookie.setHttpOnly(jwtProperties.cookie().httpOnly());

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

    public String getRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(getRefreshCookieName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
