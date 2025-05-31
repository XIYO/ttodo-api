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

public Cookie createJwtCookie(String jwtToken) {
  return createAccessTokenCookie(jwtProperties.accessToken().cookie(), jwtToken, jwtProperties.expiration());
}

public Cookie createRefreshCookie(String refreshToken) {
  return createRefreshTokenCookie(jwtProperties.refreshToken().cookie(), refreshToken, jwtProperties.refreshExpiration());
}

private Cookie createAccessTokenCookie(JwtProperties.CookieProperties cookieProps, String token, int maxAge) {
  Cookie cookie = new Cookie(cookieProps.name(), token);
  setCommonCookieProperties(cookie, cookieProps, maxAge);
  return cookie;
}

private Cookie createRefreshTokenCookie(JwtProperties.CookieProperties cookieProps, String token, int maxAge) {
  Cookie cookie = new Cookie(cookieProps.name(), token);
  setCommonCookieProperties(cookie, cookieProps, maxAge);
  return cookie;
}

private void setCommonCookieProperties(Cookie cookie, JwtProperties.CookieProperties cookieProps, int maxAge) {
  cookie.setPath(cookieProps.path());
  cookie.setSecure(cookieProps.secure());
  cookie.setHttpOnly(cookieProps.httpOnly());
  String domain = cookieProps.domain();
  if (domain != null && ! domain.equalsIgnoreCase("localhost") && ! domain.isBlank()) {
    cookie.setDomain(domain);
  }
  cookie.setMaxAge(maxAge);
  // Note: sameSite는 현재 jakarta.servlet.http.Cookie에서 직접 지원하지 않음
}

public void zeroAge(Cookie cookie) {
  cookie.setMaxAge(0);
}

public String getRefreshToken(HttpServletRequest request) {
  if (request.getCookies() == null) return null;
  String refreshCookieName = jwtProperties.refreshToken().cookie().name();
  for (Cookie cookie : request.getCookies()) {
    if (cookie.getName().equals(refreshCookieName)) {
      return cookie.getValue();
    }
  }
  return null;
}
}
