package point.zzicback.auth.security.resolver;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.web.*;
import org.springframework.stereotype.Component;
import point.zzicback.auth.config.properties.JwtProperties;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiBearerTokenResolver implements BearerTokenResolver {
  private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
  private final JwtProperties jwtProperties;

  @Override
  public String resolve(HttpServletRequest request) {
    String token = defaultResolver.resolve(request);
    if (token != null) {
      log.info("Token found in Authorization header");
      return token;
    }

    log.info("Token not found in Authorization header, checking cookies");
    if (request.getCookies() != null) {
      String cookieToken = Arrays.stream(request.getCookies())
              .filter(cookie -> jwtProperties.accessToken().cookie().name().equals(cookie.getName()))
              .findFirst()
              .map(Cookie::getValue)
              .orElse(null);

      if (cookieToken != null) {
        log.info("Token found in cookies");
      } else {
        log.info("Token not found in cookies either");
      }

      return cookieToken;
    }

    log.info("No cookies found in request");
    return null;
  }
}
