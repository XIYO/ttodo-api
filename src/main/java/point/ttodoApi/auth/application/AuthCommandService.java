package point.ttodoApi.auth.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.auth.application.command.*;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.user.application.*;
import point.ttodoApi.user.application.command.CreateUserCommand;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;

import java.util.List;

/**
 * Auth Command Service
 * TTODO 아키텍처 패턴: Command(쓰기) 처리 전용 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class AuthCommandService {

  private static final String USER_ROLE = "ROLE_USER";

  private final UserCommandService userCommandService;
  private final UserQueryService userQueryService;
  private final PasswordEncoder passwordEncoder;  // 로그인 검증용
  private final TokenService tokenService;
  private final ProfileService profileService;
  private final point.ttodoApi.auth.application.mapper.AuthApplicationMapper authMapper;

  /**
   * 회원가입 및 자동 로그인
   */
  public AuthResult signUp(@Valid SignUpCommand command) {
    // 중복 이메일 검증은 UserCommandService.createUser에서 처리됨 (UniqueEmail 어노테이션)

    // 회원 생성 - 평문 패스워드 전달, User 도메인에서 암호화 처리
    UserResult userResult = userCommandService.createUser(
        authMapper.toCreateUserCommand(command)
    );

    // 토큰 발급
    TokenService.TokenResult tokenResult = tokenService.createTokenPair(
            userResult.id().toString(),
            command.deviceId(),
            userResult.email(),
            userResult.nickname(),
            List.of(new SimpleGrantedAuthority(USER_ROLE))
    );

    // 매퍼 활용해서 AuthResult 생성
    return authMapper.toAuthResult(userResult, tokenResult);
  }

  /**
   * 로그인
   */
  public AuthResult signIn(@Valid SignInCommand command) {
    log.debug("Processing sign in for email: {}", command.email());

    // TTODO 아키텍처 패턴: Query 서비스 사용
    User user = userQueryService.findUserEntityByEmail(command.email())
            .orElseThrow(() -> new BusinessException(point.ttodoApi.shared.error.ErrorCode.INVALID_CREDENTIALS));

    // 패스워드 검증
    if (!passwordEncoder.matches(command.password(), user.getPassword()))
      throw new BusinessException(point.ttodoApi.shared.error.ErrorCode.INVALID_CREDENTIALS);

    // Profile에서 nickname 가져오기
    Profile userProfile = profileService.getProfile(user.getId());

    // 토큰 발급
    TokenService.TokenResult tokenResult = tokenService.createTokenPair(
            user.getId().toString(),
            command.deviceId(),
            user.getEmail(),
            userProfile.getNickname(),
            List.of(new SimpleGrantedAuthority(USER_ROLE))
    );

    // 매퍼 활용해서 AuthResult 생성
    return authMapper.toAuthResultFromUser(user, userProfile, tokenResult);
  }

  /**
   * 로그아웃
   */
  public void signOut(@Valid SignOutCommand command) {
    log.debug("Processing sign out for device: {}", command.deviceId());

    tokenService.deleteByToken(command.refreshToken());

    log.debug("Sign out completed for device: {}", command.deviceId());
  }

  /**
   * 토큰 갱신
   */
  public AuthResult refreshToken(@Valid RefreshTokenCommand command) {
    log.debug("Processing token refresh for device: {}", command.deviceId());

    if (!tokenService.isValidRefreshToken(command.deviceId(), command.refreshToken()))
      throw new BusinessException("유효하지 않은 리프레시 토큰입니다.");

    TokenService.TokenPair tokenPair = tokenService.refreshTokens(
            command.deviceId(),
            command.refreshToken()
    );

    log.debug("Token refresh completed for device: {}", command.deviceId());

    return AuthResult.ofTokenOnly(
            tokenPair.accessToken(),
            tokenPair.refreshToken(),
            command.deviceId()
    );
  }
}