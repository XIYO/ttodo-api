package point.zzicback.auth.application;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.auth.domain.AuthenticatedMember;
import point.zzicback.auth.jwt.TokenService;
import point.zzicback.auth.util.CookieUtil;
import point.zzicback.auth.util.JwtUtil;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
private final JwtUtil jwtUtil;
private final CookieUtil cookieUtil;
private final TokenService tokenService;

public TokenResult generateTokens(AuthenticatedMember member) {
  String deviceId = UUID.randomUUID().toString();
  String accessToken = jwtUtil.generateAccessToken(member.id(), member.email(), member.nickname());
  String refreshToken = jwtUtil.generateRefreshToken(member.id(), deviceId);
  tokenService.save(deviceId, refreshToken);
  return new TokenResult(accessToken, refreshToken, deviceId);
}

public void setTokenCookies(HttpServletResponse response, TokenResult tokens) {
  response.addCookie(cookieUtil.createJwtCookie(tokens.accessToken()));
  response.addCookie(cookieUtil.createRefreshCookie(tokens.refreshToken()));
}

public void authenticateWithCookies(AuthenticatedMember member, HttpServletResponse response) {
  TokenResult tokens = generateTokens(member);
  setTokenCookies(response, tokens);
}

public void signOut(HttpServletRequest request, HttpServletResponse response) {
  clearTokenCookies(response);
  String refreshToken = cookieUtil.getRefreshToken(request);
  if (refreshToken != null) {
    tokenService.deleteByToken(refreshToken);
  }
}

public void clearTokenCookies(HttpServletResponse response) {
  Cookie expiredAccessCookie = cookieUtil.createJwtCookie("");
  Cookie expiredRefreshCookie = cookieUtil.createRefreshCookie("");
  cookieUtil.zeroAge(expiredAccessCookie);
  cookieUtil.zeroAge(expiredRefreshCookie);
  response.addCookie(expiredAccessCookie);
  response.addCookie(expiredRefreshCookie);
}

public boolean refreshTokensIfNeeded(HttpServletRequest request, HttpServletResponse response) {
  String refreshToken = cookieUtil.getRefreshToken(request);
  if (refreshToken == null)
    return false;
  try {
    String deviceId = jwtUtil.extractClaim(refreshToken, "device");
    TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);
    response.addCookie(cookieUtil.createJwtCookie(newTokens.accessToken()));
    response.addCookie(cookieUtil.createRefreshCookie(newTokens.refreshToken()));
    return true;
  } catch (Exception e) {
    clearTokenCookies(response);
    return false;
  }
}

public record TokenResult(String accessToken, String refreshToken, String deviceId) {
}
}
