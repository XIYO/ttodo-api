package point.zzicback.auth.security.resolver;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.web.*;
import org.springframework.stereotype.Component;
import point.zzicback.auth.config.properties.JwtProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiBearerTokenResolver implements BearerTokenResolver {
  private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
  private final JwtProperties jwtProperties;

  @Override
  public String resolve(HttpServletRequest request) {
    // 요청 URL 로깅
    log.info("Processing request for URL: {}", request.getRequestURL().toString());

    // 헤더 정보 로깅
    logHeaders(request);

    String token = defaultResolver.resolve(request);
    if (token != null) {
      log.info("Token found in Authorization header");
      return token;
    }

    log.info("Token not found in Authorization header, checking cookies");
    if (request.getCookies() != null) {
      log.info("Found {} cookies in request", request.getCookies().length);

      // 모든 쿠키 이름 로깅
      String cookieNames = Arrays.stream(request.getCookies())
          .map(Cookie::getName)
          .collect(Collectors.joining(", "));
      log.info("Available cookies: {}", cookieNames);

      // JWT 쿠키 설정 이름 로깅
      log.info("Looking for JWT cookie named: {}", jwtProperties.accessToken().cookie().name());

      String cookieToken = Arrays.stream(request.getCookies())
              .filter(cookie -> jwtProperties.accessToken().cookie().name().equals(cookie.getName()))
              .findFirst()
              .map(Cookie::getValue)
              .orElse(null);

      if (cookieToken != null) {
        log.info("Token found in cookies");
      } else {
        log.info("Token not found in cookies either. JWT cookie '{}' is missing",
                jwtProperties.accessToken().cookie().name());
      }

      return cookieToken;
    }

    log.info("No cookies found in request");
    return null;
  }

  private void logHeaders(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames != null) {
      Collections.list(headerNames).forEach(headerName -> {
        String headerValue = request.getHeader(headerName);
        // 민감한 정보는 마스킹
        if (headerName.toLowerCase().contains("authorization")) {
          headerValue = "****MASKED****";
        }
        log.info("Header: {} = {}", headerName, headerValue);
      });
    }
  }
}
