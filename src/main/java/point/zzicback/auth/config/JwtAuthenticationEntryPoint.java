package point.zzicback.auth.config;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import point.zzicback.auth.application.TokenService;
import point.zzicback.auth.presentation.CookieService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final TokenService tokenService;
  private final CookieService cookieService;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
    Throwable cause = authException.getCause();
    if (!(cause instanceof JwtValidationException) && cause != null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    
    boolean refreshed = refreshTokensIfNeeded(request, response);
    if (!refreshed) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    
    String uri = request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    response.setHeader(HttpHeaders.LOCATION, uri);
  }

  private boolean refreshTokensIfNeeded(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieService.getRefreshToken(request)
            .ifPresentOrElse(() => null);

    try {
      String deviceId = tokenService.extractClaim(refreshToken, TokenService.DEVICE_CLAIM);
      TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);
      
      Cookie accessCookie = cookieService.createJwtCookie(newTokens.accessToken());
      Cookie refreshCookie = cookieService.createRefreshCookie(newTokens.refreshToken());
      
      response.addCookie(accessCookie);
      response.addCookie(refreshCookie);
      
      return true;
    } catch (Exception _) {
      return false;
    }
  }
}
