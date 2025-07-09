package point.ttodoApi.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.auth.domain.MemberPrincipal;
import point.ttodoApi.auth.presentation.dto.request.*;
import point.ttodoApi.common.error.BusinessException;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.CreateMemberCommand;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.presentation.dto.response.MemberResponse;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.todo.config.TodoInitializer;

import java.util.List;

@Tag(name = "사용자 인증 및 회원 관리", description = "회원 가입, 로그인, 로그아웃, 토큰 갱신 등 인증 및 인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Transactional
public class AuthController {
  private static final String USER_ROLE = "ROLE_USER";
  private static final String ANON_EMAIL = "anon@ttodo.dev";
  
  private final MemberService memberService;
  private final ProfileService profileService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final CookieService cookieService;
  private final TodoInitializer todoInitializer;

  @Operation(summary = "사인-업 및 사인-인", description = "사인-업을 진행하고 즉시 사인-인하여 JWT/리프레시 토큰을 쿠키로 발급합니다.")
  @ApiResponse(responseCode = "200", description = "사인-업 및 사인-인 성공, 쿠키에 토큰 발급")
  @ApiResponse(responseCode = "400", description = "잘못된 요청")
  @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
  @PostMapping("/sign-up")
  public void signUpAndIn(@Valid @RequestBody SignUpRequest request, HttpServletResponse response) {
    CreateMemberCommand signUpCommand = new CreateMemberCommand(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.nickname(),
            request.introduction());
    memberService.createMember(signUpCommand);
    Member member = authenticateMember(request.email(), request.password());
    Profile profile = profileService.getProfile(member.getId());
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
    MemberPrincipal memberPrincipal = MemberPrincipal.from(member, profile.getTimeZone(), profile.getLocale(), authorities);
    authenticateWithCookies(memberPrincipal, response);
  }

  @Operation(
      summary = "사인-인", 
      description = "사인-인을 진행하고 JWT/리프레시 토큰을 쿠키로 발급합니다.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "사인-인 정보",
          required = true,
          content = @io.swagger.v3.oas.annotations.media.Content(
              mediaType = "application/json",
              schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SignInRequest.class),
              examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                  name = "로그인 예시",
                  value = """
                      {
                        "email": "anon@ttodo.dev",
                        "password": ""
                      }
                      """
              )
          )
      )
  )
  @ApiResponse(responseCode = "200", description = "사인-인 성공, 쿠키에 토큰 발급")
  @ApiResponse(responseCode = "401", description = "인증 실패")
  @PostMapping("/sign-in")
  @Transactional(readOnly = true)
  public void signIn(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
    Member member = authenticateMember(request.email(), request.password());
    Profile profile = profileService.getProfile(member.getId());
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
    MemberPrincipal memberPrincipal = MemberPrincipal.from(member, profile.getTimeZone(), profile.getLocale(), authorities);
    
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
  public void refresh() {
  }

  @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
  @ApiResponse(responseCode = "401", description = "인증 실패")
  @GetMapping("/me")
  @Transactional(readOnly = true)
  public MemberResponse getCurrentUser(Authentication authentication) {
    MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
    Member member = memberService.findVerifiedMember(principal.id());
    return MemberResponse.from(member);
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
    } catch (Exception e) {
      throw new BusinessException("이메일 또는 패스워드가 올바르지 않습니다.");
    }
  }

  private void authenticateWithCookies(MemberPrincipal member, HttpServletResponse response) {
    TokenService.TokenResult tokens = tokenService.generateTokens(member);
    
    cookieService.setJwtCookie(response, tokens.accessToken());
    cookieService.setRefreshCookie(response, tokens.refreshToken());
  }
}
