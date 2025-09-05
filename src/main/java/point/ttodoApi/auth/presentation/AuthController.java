package point.ttodoApi.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.auth.domain.MemberPrincipal;
import point.ttodoApi.auth.presentation.dto.request.SignInRequest;
import point.ttodoApi.auth.presentation.dto.request.SignUpRequest;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.CreateMemberCommand;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;

import java.util.List;

@Tag(name = "인증(Authentication)", description = "회원가입, 로그인, 로그아웃, 토큰 갱신 등 사용자 인증 관련 API를 제공합니다. JWT 기반 인증을 사용하며, 액세스 토큰과 리프레시 토큰을 쿠키로 관리합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Transactional
public class AuthController {
  private static final String USER_ROLE = "ROLE_USER";
  private static final String ANON_EMAIL_FALLBACK = "anon@ttodo.dev";
  
  private final MemberService memberService;
  private final ProfileService profileService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;
  private final point.ttodoApi.shared.config.properties.AppProperties appProperties;
  private final CookieService cookieService;

  @Operation(
      summary = "회원가입 및 자동 로그인", 
      description = "새로운 회원을 등록하고 자동으로 로그인 처리합니다. 회원가입이 완료되면 즉시 JWT 액세스 토큰과 리프레시 토큰이 쿠키로 발급됩니다. 익명 사용자는 이메일 'anon@ttodo.dev'와 빈 패스워드로 회원가입 가능합니다."
  )
  @ApiResponse(responseCode = "200", description = "회원가입 성공 및 자동 로그인 완료")
  @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (이메일 형식 오류, 필수값 누락 등)")
  @ApiResponse(responseCode = "409", description = "이미 사용중인 이메일 주소")
  @PostMapping(value = "/sign-up", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  public void signUpAndIn(@Valid SignUpRequest request, HttpServletResponse response) {
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
      summary = "로그인", 
      description = "이메일과 패스워드로 로그인합니다. 인증 성공 시 JWT 액세스 토큰과 리프레시 토큰이 쿠키로 발급됩니다. 익명 사용자는 'anon@ttodo.dev' 이메일과 빈 패스워드로 로그인 가능합니다.\n\n" +
                    "발급되는 쿠키:\n" +
                    "- jwt-token: 액세스 토큰 (유효기간: 30분)\n" +
                    "- refresh-token: 리프레시 토큰 (유효기간: 7일)",
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
  @ApiResponse(responseCode = "200", description = "로그인 성공")
  @ApiResponse(responseCode = "400", description = "입력값 검증 실패")
  @ApiResponse(responseCode = "401", description = "이메일 또는 패스워드가 일치하지 않음")
  @PostMapping(value = "/sign-in", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  @Transactional(readOnly = true)
  public void signIn(@Valid SignInRequest request, HttpServletResponse response) {
    Member member = authenticateMember(request.email(), request.password());
    Profile profile = profileService.getProfile(member.getId());
    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
    MemberPrincipal memberPrincipal = MemberPrincipal.from(member, profile.getTimeZone(), profile.getLocale(), authorities);
    
    authenticateWithCookies(memberPrincipal, response);
  }

  @Operation(
      summary = "로그아웃", 
      description = "현재 사용자를 로그아웃 처리합니다. 서버에서 리프레시 토큰을 삭제하고, 클라이언트의 JWT 쿠키와 리프레시 토큰 쿠키를 만료시킵니다."
  )
  @ApiResponse(responseCode = "200", description = "로그아웃 성공")
  @PostMapping(value = "/sign-out", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  public void signOut(@CookieValue(name = "refresh-token", required = false) String refreshToken, HttpServletResponse response) {
    cookieService.setExpiredJwtCookie(response);
    if (refreshToken != null) {
      tokenService.deleteByToken(refreshToken);
      cookieService.setExpiredRefreshCookie(response);
    }
  }

  @Operation(
      summary = "액세스 토큰 갱신", 
      description = "리프레시 토큰을 사용하여 만료된 액세스 토큰을 갱신합니다. 리프레시 토큰은 쿠키에서 자동으로 읽어옵니다. 이 엔드포인트는 JWT 인터셉터에서 자동으로 호출됩니다."
  )
  @ApiResponse(responseCode = "200", description = "토큰 갱신 성공, 새로운 액세스 토큰이 쿠키로 발급됨")
  @ApiResponse(responseCode = "401", description = "리프레시 토큰이 만료되었거나 유효하지 않음")
  @PostMapping(value = "/refresh", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
  public void refresh() {
  }


  private Member authenticateMember(String email, String password) {
    Member member;
    
    try {
      member = memberService.findByEmailOrThrow(email);
    } catch (BusinessException e) {
      // 회원을 찾을 수 없는 경우 - 보안상 동일한 메시지 반환
      throw new BusinessException("이메일 또는 패스워드가 올바르지 않습니다.");
    }
    
    // 소셜 로그인 사용자 (패스워드 없음)
    if (member.getPassword() == null || member.getPassword().isEmpty()) {
      return member;
    }
    
    // 일반 로그인 사용자 - 패스워드 검증
    if (password != null && passwordEncoder.matches(password, member.getPassword())) {
      return member;
    }
    
    throw new BusinessException("이메일 또는 패스워드가 올바르지 않습니다.");
  }

  private void authenticateWithCookies(MemberPrincipal member, HttpServletResponse response) {
    TokenService.TokenResult tokens = tokenService.generateTokens(member);
    
    cookieService.setJwtCookie(response, tokens.accessToken());
    cookieService.setRefreshCookie(response, tokens.refreshToken());
  }
  
  @Operation(
      summary = "개발 환경 테스트 토큰 생성", 
      description = "개발 환경에서 Swagger 테스트용 영구 토큰을 생성합니다. 이 토큰은 만료 시간이 없습니다. **프로덕션 환경에서는 사용 불가**"
  )
  @ApiResponse(responseCode = "200", description = "테스트 토큰 생성 성공")
  @GetMapping("/dev-token")
  @org.springframework.context.annotation.Profile("!prod")
  public java.util.Map<String, String> getDevToken() {
    // 시스템 익명 사용자 정보 사용 (초기 데이터로 생성됨)
    String domain = appProperties.getUserDomain() == null ? "ttodo.dev" : appProperties.getUserDomain();
    String testUserId = point.ttodoApi.shared.constants.SystemConstants.SystemUsers.ANON_USER_ID.toString();
    String testEmail = "anon@" + domain;
    String testNickname = point.ttodoApi.shared.constants.SystemConstants.SystemUsers.ANON_USER_NICKNAME;
    String testTimeZone = "Asia/Seoul";
    String testLocale = "ko-KR";
    
    // 만료 없는 토큰 생성 (neverExpire = true)
    String token = tokenService.generateAccessToken(testUserId, testEmail, testNickname, testTimeZone, testLocale, true);
    
    return java.util.Map.of(
        "token", token,
        "usage", "Swagger의 Authorize 버튼을 클릭하고 이 토큰을 붙여넣으세요 (Bearer 접두사 없이)",
        "userId", testUserId,
        "email", testEmail,
        "nickname", testNickname,
        "expiresIn", "NEVER (만료 없음)"
    );
  }
}
