package point.zzicback.auth.presentation;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.auth.config.properties.JwtProperties;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CookieService {
  private final JwtProperties jwtProperties;

  public Cookie createJwtCookie(String jwtToken) {
    return createCookie(jwtProperties.accessToken().cookie(), jwtToken, jwtProperties.accessToken().expiration());
  }

  public Cookie createRefreshCookie(String refreshToken) {
    return createCookie(jwtProperties.refreshToken().cookie(), refreshToken, jwtProperties.refreshToken().expiration());
  }

  public Cookie createExpiredJwtCookie() {
    return createCookie(jwtProperties.accessToken().cookie(), "", 0);
  }

  public Cookie createExpiredRefreshCookie() {
    return createCookie(jwtProperties.refreshToken().cookie(), "", 0);
  }

  public Optional<String> getRefreshToken(Cookie[] cookies) {
    return cookies == null ? Optional.empty() :
        Arrays.stream(cookies)
            .filter(cookie -> jwtProperties.refreshToken().cookie().name().equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue);
  }

  private Cookie createCookie(JwtProperties.CookieProperties props, String token, int maxAge) {
    Cookie cookie = new Cookie(props.name(), token);
    cookie.setPath(props.path());
    cookie.setSecure(props.secure());
    cookie.setHttpOnly(props.httpOnly());
    cookie.setMaxAge(maxAge);
    cookie.setDomain(props.domain());
    return cookie;
  }
}
