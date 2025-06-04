package point.zzicback.auth.presentation;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import point.zzicback.auth.config.properties.JwtProperties;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CookieService {
  private static final String SET_COOKIE_HEADER = "Set-Cookie";
  private final JwtProperties jwtProperties;

  public void setJwtCookie(HttpServletResponse response, String jwtToken) {
    ResponseCookie cookie = createResponseCookie(jwtProperties.accessToken().cookie(), jwtToken, jwtProperties.accessToken().expiration());
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
    ResponseCookie cookie = createResponseCookie(jwtProperties.refreshToken().cookie(), refreshToken, jwtProperties.refreshToken().expiration());
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public void setExpiredJwtCookie(HttpServletResponse response) {
    ResponseCookie cookie = createResponseCookie(jwtProperties.accessToken().cookie(), "", 0);
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public void setExpiredRefreshCookie(HttpServletResponse response) {
    ResponseCookie cookie = createResponseCookie(jwtProperties.refreshToken().cookie(), "", 0);
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public Optional<String> getRefreshToken(Cookie[] cookies) {
    return cookies == null ? Optional.empty() :
        Arrays.stream(cookies)
            .filter(cookie -> jwtProperties.refreshToken().cookie().name().equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue);
  }

  private ResponseCookie createResponseCookie(JwtProperties.CookieProperties props, String value, int maxAge) {
    ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(props.name(), value)
        .path(props.path())
        .maxAge(Duration.ofSeconds(maxAge))
        .secure(props.secure())
        .sameSite(props.sameSite())
        .httpOnly(props.httpOnly());
    
    if (props.domain() != null && !props.domain().isEmpty()) {
      builder.domain(props.domain());
    }
    
    if (props.sameSite() != null && !props.sameSite().isEmpty()) {
      builder.sameSite(props.sameSite());
    }
    
    return builder.build();
  }
}
