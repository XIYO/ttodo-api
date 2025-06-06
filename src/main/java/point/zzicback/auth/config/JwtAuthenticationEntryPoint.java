package point.zzicback.auth.config;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import point.zzicback.auth.application.TokenService;
import point.zzicback.auth.presentation.CookieService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final TokenService tokenService;
  private final CookieService cookieService;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    log.info("Authentication failure detected for URI: {}", request.getRequestURI());

    Throwable cause = authException.getCause();
    if (!(cause instanceof JwtValidationException) && cause != null) {
      log.info("Non-JWT validation failure: {}", cause.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (cause instanceof JwtValidationException) {
      JwtValidationException jwtException = (JwtValidationException) cause;
      log.info("JWT validation failed. Reason: {}, Errors: {}",
              cause.getMessage(),
              jwtException.getErrors());
    }

    boolean refreshed = refreshTokensIfNeeded(request, response);
    if (!refreshed) {
      log.info("Failed to refresh tokens. Setting response as UNAUTHORIZED.");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      cookieService.setExpiredJwtCookie(response);
      cookieService.setExpiredRefreshCookie(response);
      return;
    }

    String uri = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    log.info("Tokens successfully refreshed. Redirecting to: {}", uri);
    response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    response.setHeader(HttpHeaders.LOCATION, uri);
  }

  private boolean refreshTokensIfNeeded(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieService.getRefreshToken(request.getCookies())
            .orElse(null);
    
    if (refreshToken == null) {
      log.info("No refresh token found in cookies");
      return false;
    }

    log.info("Refresh token found. Attempting to refresh tokens...");
    try {
      String deviceId = tokenService.extractClaim(refreshToken, TokenService.DEVICE_CLAIM);
      log.info("Extracted device ID: {}", deviceId);

      TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);
      log.info("Tokens successfully refreshed for device ID: {}", deviceId);

      cookieService.setJwtCookie(response, newTokens.accessToken());
      cookieService.setRefreshCookie(response, newTokens.refreshToken());

      return true;
    } catch (Exception e) {
      log.error("Failed to refresh tokens: {}", e.getMessage(), e);
      tokenService.deleteByToken(refreshToken);
      
      log.info("Deleting invalid refresh token and setting expired cookies");
      cookieService.setExpiredJwtCookie(response);
      cookieService.setExpiredRefreshCookie(response);
      return false;
    }
  }
}
