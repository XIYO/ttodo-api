package point.ttodoApi.auth.application.mapper;

import org.mapstruct.*;
import point.ttodoApi.auth.application.command.SignUpCommand;
import point.ttodoApi.auth.application.result.AuthResult;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.config.MapStructConfig;
import point.ttodoApi.user.application.command.CreateUserCommand;
import point.ttodoApi.user.domain.User;

@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface AuthApplicationMapper {

  /**
   * SignUpCommand를 CreateUserCommand로 변환 (평문 패스워드 전달)
   * TTODO 규칙: DTO 간 변환은 무조건 매퍼로
   */
  default CreateUserCommand toCreateUserCommand(SignUpCommand signUpCommand) {
    return new CreateUserCommand(
            signUpCommand.email(),
            signUpCommand.password(),  // 평문 패스워드 전달 - User 도메인에서 암호화 처리
            signUpCommand.nickname(),
            signUpCommand.introduction()
    );
  }

  /**
   * User와 Profile을 사용한 AuthResult 생성 (signIn용)
   */
  default AuthResult toAuthResultFromUser(User user, Profile profile,
                                          point.ttodoApi.auth.application.TokenService.TokenResult tokenResult) {
    return new AuthResult(
            tokenResult.accessToken(),
            tokenResult.refreshToken(),
            tokenResult.deviceId(),
            user.getId(),
            user.getEmail(),
            profile.getNickname()
    );
  }

  /**
   * UserResult와 토큰 정보로 AuthResult 생성 (signUp용)
   */
  default AuthResult toAuthResult(point.ttodoApi.user.application.result.UserResult userResult,
                                  point.ttodoApi.auth.application.TokenService.TokenResult tokenResult) {
    return new AuthResult(
            tokenResult.accessToken(),
            tokenResult.refreshToken(),
            tokenResult.deviceId(),
            userResult.id(),
            userResult.email(),
            userResult.nickname()
    );
  }

}