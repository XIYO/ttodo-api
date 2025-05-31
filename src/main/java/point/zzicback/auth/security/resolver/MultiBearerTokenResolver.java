package point.zzicback.auth.security.resolver;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.*;
import org.springframework.stereotype.Component;
import point.zzicback.auth.config.properties.JwtProperties;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MultiBearerTokenResolver implements BearerTokenResolver {
private final DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
private final JwtProperties jwtProperties;

@Override
public String resolve(HttpServletRequest request) {
  String token = defaultResolver.resolve(request);
  if (token != null) {
    return token;
  }
  if (request.getCookies() != null) {
    return Arrays.stream(request.getCookies())
            .filter(cookie -> jwtProperties.accessToken().cookie().name().equals(cookie.getName())).findFirst()
            .map(Cookie::getValue).orElse(null);
  }
  return null;
}
}
