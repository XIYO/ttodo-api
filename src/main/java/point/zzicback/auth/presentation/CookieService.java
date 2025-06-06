package point.zzicback.auth.presentation;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import point.zzicback.auth.config.properties.JwtProperties;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CookieService {
  private static final String SET_COOKIE_HEADER = "Set-Cookie";
  private final JwtProperties jwtProperties;

  public void setJwtCookie(HttpServletResponse response, String jwtToken) {
    log.info("Setting JWT cookie with name: {}", jwtProperties.accessToken().cookie().name());
    ResponseCookie cookie = createResponseCookie(jwtProperties.accessToken().cookie(), jwtToken, jwtProperties.accessToken().expiration());
    log.info("Created JWT cookie: {}", sanitizeCookieLog(cookie.toString()));
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
    response.setHeader("Authorization", "Bearer " + jwtToken);
    log.info("JWT cookie and Authorization header set in response");
  }

  public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
    log.info("Setting refresh cookie with name: {}", jwtProperties.refreshToken().cookie().name());
    ResponseCookie cookie = createResponseCookie(jwtProperties.refreshToken().cookie(), refreshToken, jwtProperties.refreshToken().expiration());
    log.info("Created refresh cookie: {}", sanitizeCookieLog(cookie.toString()));
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
    response.setHeader("Authorization-refresh", refreshToken);
    log.info("Refresh cookie and Authorization-refresh header set in response");
  }

  public void setExpiredJwtCookie(HttpServletResponse response) {
    log.info("Setting expired JWT cookie");
    ResponseCookie cookie = createResponseCookie(jwtProperties.accessToken().cookie(), "", 0);
    log.info("Created expired JWT cookie: {}", cookie.toString());
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public void setExpiredRefreshCookie(HttpServletResponse response) {
    log.info("Setting expired refresh cookie");
    ResponseCookie cookie = createResponseCookie(jwtProperties.refreshToken().cookie(), "", 0);
    log.info("Created expired refresh cookie: {}", cookie.toString());
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public Optional<String> getRefreshToken(Cookie[] cookies) {
    if (cookies == null) {
      log.warn("No cookies found in request while looking for refresh token");
      return Optional.empty();
    }

    log.info("Searching for refresh token in cookies. Cookie name to find: {}", jwtProperties.refreshToken().cookie().name());
    log.info("Available cookies: {}", Arrays.stream(cookies).map(Cookie::getName).toArray());

    Optional<String> token = Arrays.stream(cookies)
        .filter(cookie -> jwtProperties.refreshToken().cookie().name().equals(cookie.getName()))
        .findFirst()
        .map(Cookie::getValue);

    if (token.isPresent()) {
      log.info("Refresh token found in cookies");
    } else {
      log.warn("Refresh token not found in cookies");
    }

    return token;
  }

  private ResponseCookie createResponseCookie(JwtProperties.CookieProperties props, String value, int maxAge) {
    log.info("Creating cookie with name: {}, path: {}, maxAge: {}s, secure: {}, httpOnly: {}, sameSite: {}, domain: {}",
        props.name(), props.path(), maxAge, props.secure(), props.httpOnly(),
        props.sameSite() != null ? props.sameSite() : "null",
        props.domain() != null ? props.domain() : "null");

    ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(props.name(), value)
        .path(props.path())
        .maxAge(Duration.ofSeconds(maxAge))
        .secure(props.secure())
        .httpOnly(props.httpOnly());
    
    if (props.domain() != null && !props.domain().isEmpty()) {
      builder.domain(props.domain());
    }
    
    if (props.sameSite() != null && !props.sameSite().isEmpty()) {
      builder.sameSite(props.sameSite());
    }
    
    return builder.build();
  }

  private String sanitizeCookieLog(String cookieString) {
    // 토큰 값을 마스킹하여 로그에 노출되지 않도록 함
    return cookieString.replaceAll("=([^;]*)(?=;|$)", "=****MASKED****");
  }
}
