package point.ttodoApi.shared.config.auth;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.auth.presentation.CookieService;
import point.ttodoApi.shared.error.BusinessException;

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
      if (deviceId == null) {
        log.warn("Could not extract device ID from refresh token");
        cookieService.setExpiredJwtCookie(response);
        cookieService.setExpiredRefreshCookie(response);
        return false;
      }
      log.info("Extracted device ID: {}", deviceId);

      TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);
      log.info("Tokens successfully refreshed for device ID: {}", deviceId);

      cookieService.setJwtCookie(response, newTokens.accessToken());
      cookieService.setRefreshCookie(response, newTokens.refreshToken());

      return true;
    } catch (BusinessException e) {
      log.info("Business exception during token refresh: {}", e.getMessage());
      tokenService.deleteByToken(refreshToken);
      cookieService.setExpiredJwtCookie(response);
      cookieService.setExpiredRefreshCookie(response);
      return false;
    } catch (Exception e) {
      log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
      // 예상치 못한 에러의 경우 토큰을 삭제하지 않고 500 에러 반환
      // 일시적인 문제일 수 있으므로 재시도 기회를 줌
      return false;
    }
  }
}
