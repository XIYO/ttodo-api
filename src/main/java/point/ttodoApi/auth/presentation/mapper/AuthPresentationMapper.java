package point.ttodoApi.auth.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.ttodoApi.auth.application.command.RefreshTokenCommand;
import point.ttodoApi.auth.application.command.SignInCommand;
import point.ttodoApi.auth.application.command.SignOutCommand;
import point.ttodoApi.auth.application.command.SignUpCommand;
import point.ttodoApi.auth.application.query.DevTokenQuery;
import point.ttodoApi.auth.presentation.dto.request.SignInRequest;
import point.ttodoApi.auth.presentation.dto.request.SignUpRequest;
import point.ttodoApi.shared.config.MapStructConfig;

/**
 * Auth Presentation Layer Mapper
 * Request DTOs → Command/Query 객체 변환
 * TTODO 아키텍처 패턴: 수동 Command 생성을 MapStruct로 대체
 */
@Mapper(config = MapStructConfig.class)
@SuppressWarnings("NullableProblems")
public interface AuthPresentationMapper {

    /**
     * SignUpRequest → SignUpCommand 변환
     * HTML 스크립트 정제 처리는 Controller에서 수행 후 전달
     */
    @Mapping(target = "deviceId", constant = "default-device-id")
    SignUpCommand toCommand(SignUpRequest request, String sanitizedNickname, String sanitizedIntroduction);

    /**
     * SignInRequest → SignInCommand 변환
     */
    SignInCommand toCommand(SignInRequest request, String deviceId);

    /**
     * SignOut 파라미터 → SignOutCommand 변환
     */
    SignOutCommand toSignOutCommand(String deviceId, String refreshToken);

    /**
     * RefreshToken 파라미터 → RefreshTokenCommand 변환
     */
    RefreshTokenCommand toRefreshTokenCommand(String deviceId, String refreshToken);

    /**
     * DevToken Query 생성
     */
    DevTokenQuery toDevTokenQuery(String deviceId);
}