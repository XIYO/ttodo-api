package point.zzicback.auth.presentation;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import point.zzicback.auth.config.properties.JwtProperties;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CookieService {
  private final JwtProperties jwtProperties;

  public Cookie createJwtCookie(String jwtToken) {
    return createCookie(jwtProperties.accessToken().cookie(), jwtToken, jwtProperties.expiration());
  }

  public Cookie createRefreshCookie(String refreshToken) {
    return createCookie(jwtProperties.refreshToken().cookie(), refreshToken, jwtProperties.refreshExpiration());
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

  public Optional<String> getRefreshToken(HttpServletRequest request) {
    return request.getCookies() == null ? null : 
      Arrays.stream(request.getCookies())
        .filter(cookie -> jwtProperties.refreshToken().cookie().name().equals(cookie.getName()))
        .findFirst()
        .map(Cookie::getValue);
  }

  public void expireTokenCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      String accessName = jwtProperties.accessToken().cookie().name();
      String refreshName = jwtProperties.refreshToken().cookie().name();
      for (Cookie cookie : cookies) {
        String name = cookie.getName();
        if (accessName.equals(name) || refreshName.equals(name)) {
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      }
    }
  }
}
