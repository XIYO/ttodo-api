package point.zzicback.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.application.TokenService;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.auth.presentation.dto.*;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;

import java.util.List;

@Tag(name = "인증", description = "회원 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Transactional
public class AuthController {
  private static final String USER_ROLE = "ROLE_USER";
  
  private final MemberService memberService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final CookieService cookieService;

  @Operation(summary = "사인-업 및 사인-인", description = "사인-업을 진행하고 즉시 사인-인하여 JWT/리프레시 토큰을 쿠키로 발급합니다.")
  @ApiResponse(responseCode = "200", description = "사인-업 및 사인-인 성공, 쿠키에 토큰 발급")
  @ApiResponse(responseCode = "400", description = "잘못된 요청")
  @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
  @PostMapping("/sign-up")
  public void signUpAndIn(@Valid @RequestBody SignUpRequest request, HttpServletResponse response) {
    CreateMemberCommand signUpCommand = new CreateMemberCommand(request.email(), passwordEncoder.encode(request.password()), request.nickname());
    memberService.createMember(signUpCommand);
    Member member = authenticateMember(request.email(), request.password());
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
    MemberPrincipal memberPrincipal = MemberPrincipal.from(member, authorities);
    authenticateWithCookies(memberPrincipal, response);
  }

  @Operation(summary = "사인-인", description = "사인-인을 진행하고 JWT/리프레시 토큰을 쿠키로 발급합니다.")
  @ApiResponse(responseCode = "200", description = "사인-인 성공, 쿠키에 토큰 발급")
  @ApiResponse(responseCode = "401", description = "인증 실패")
  @PostMapping("/sign-in")
  @Transactional(readOnly = true)
  public void signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
    Member member = authenticateMember(request.email(), request.password());
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
    MemberPrincipal memberPrincipal = MemberPrincipal.from(member, authorities);
    authenticateWithCookies(memberPrincipal, response);
  }

  @Operation(summary = "사인-아웃", description = "사인-아웃 처리 및 토큰 만료")
  @ApiResponse(responseCode = "200", description = "사인-아웃 성공")
  @PostMapping("/sign-out")
  public void signOut(@CookieValue(name = "refresh-token", required = false) String refreshToken, HttpServletResponse response) {
    cookieService.setExpiredJwtCookie(response);
    if (refreshToken != null) {
      tokenService.deleteByToken(refreshToken);
      cookieService.setExpiredRefreshCookie(response);
    }
  }

  @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 액세스 토큰 갱신")
  @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
  @ApiResponse(responseCode = "401", description = "토큰 갱신 실패")
  @GetMapping("/refresh")
  public void refresh(@CookieValue("refresh-token") String refreshToken, HttpServletResponse response) {
    String deviceId = tokenService.extractClaim(refreshToken, TokenService.DEVICE_CLAIM);
    TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);

    cookieService.setJwtCookie(response, newTokens.accessToken());
    cookieService.setRefreshCookie(response, newTokens.refreshToken());
  }

  private Member authenticateMember(String email, String password) {
    try {
      Member member = memberService.findByEmailOrThrow(email);
      
      if (member.getPassword() == null || member.getPassword().isEmpty()) {
        return member;
      }
      
      if (password != null && passwordEncoder.matches(password, member.getPassword())) {
        return member;
      }
      
      throw new BusinessException("이메일 또는 패스워드가 올바르지 않습니다.");
    } catch (Exception _) {
      throw new BusinessException("이메일 또는 패스워드가 올바르지 않습니다.");
    }
  }

  private void authenticateWithCookies(MemberPrincipal member, HttpServletResponse response) {
    TokenService.TokenResult tokens = tokenService.generateTokens(member);
    
    cookieService.setJwtCookie(response, tokens.accessToken());
    cookieService.setRefreshCookie(response, tokens.refreshToken());
  }
}
