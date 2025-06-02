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

  @Operation(summary = "회원가입 및 로그인", description = "회원가입을 진행하고 즉시 로그인하여 JWT/리프레시 토큰을 쿠키로 발급합니다.")
  @ApiResponse(responseCode = "200", description = "회원가입 및 로그인 성공, 쿠키에 토큰 발급")
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

  @Operation(summary = "로그인", description = "로그인을 진행하고 JWT/리프레시 토큰을 쿠키로 발급합니다.")
  @ApiResponse(responseCode = "200", description = "로그인 성공, 쿠키에 토큰 발급")
  @ApiResponse(responseCode = "401", description = "인증 실패")
  @PostMapping("/sign-in")
  @Transactional(readOnly = true)
  public void signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
    Member member = authenticateMember(request.email(), request.password());
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
    MemberPrincipal memberPrincipal = MemberPrincipal.from(member, authorities);
    authenticateWithCookies(memberPrincipal, response);
  }

  @Operation(summary = "로그아웃", description = "로그아웃 처리 및 토큰 만료")
  @ApiResponse(responseCode = "200", description = "로그아웃 성공")
  @PostMapping("/sign-out")
  public void signOut(HttpServletRequest request, HttpServletResponse response) {
    cookieService.getRefreshToken(request.getCookies()).ifPresent(tokenService::deleteByToken);
    response.addCookie(cookieService.createExpiredJwtCookie());
    response.addCookie(cookieService.createExpiredRefreshCookie());
  }

  @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 액세스 토큰 갱신")
  @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
  @ApiResponse(responseCode = "401", description = "토큰 갱신 실패")
  @GetMapping("/refresh")
  public void refresh(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = cookieService.getRefreshToken(request.getCookies())
        .orElseThrow(() -> new BusinessException("리프레시 토큰이 없습니다."));
    
    String deviceId = tokenService.extractClaim(refreshToken, TokenService.DEVICE_CLAIM);
    TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);

    response.addCookie(cookieService.createJwtCookie(newTokens.accessToken()));
    response.addCookie(cookieService.createRefreshCookie(newTokens.refreshToken()));
  }

  private Member authenticateMember(String email, String password) {
    Member member = memberService.findByEmail(email);
    if (!passwordEncoder.matches(password, member.getPassword())) {
      throw new BusinessException("비밀번호가 틀렸습니다.");
    }
    return member;
  }

  private void authenticateWithCookies(MemberPrincipal member, HttpServletResponse response) {
    TokenService.TokenResult tokens = tokenService.generateTokens(member);
    
    Cookie accessCookie = cookieService.createJwtCookie(tokens.accessToken());
    Cookie refreshCookie = cookieService.createRefreshCookie(tokens.refreshToken());

    response.addCookie(accessCookie);
    response.addCookie(refreshCookie);
  }
}
