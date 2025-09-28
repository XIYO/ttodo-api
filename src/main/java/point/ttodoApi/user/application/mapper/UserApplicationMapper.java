package point.ttodoApi.user.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import point.ttodoApi.user.application.command.CreateUserCommand;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.domain.User;

/**
 * User Application Mapper
 * TTODO 아키텍처 패턴: Application Layer 매퍼
 * Domain ↔ Application DTO 변환
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface UserApplicationMapper {

    /**
     * CreateUserCommand → User 엔티티 변환
     */
    User toEntity(CreateUserCommand command);

    /**
     * User 엔티티 → UserResult 변환
     * Note: nickname은 별도로 Profile에서 가져와야 함
     */
    @Mapping(target = "nickname", ignore = true)
    UserResult toResult(User user);
    
    /**
     * User 엔티티 → UserResult 변환 (nickname 포함)
     */
    default UserResult toResult(User user, String nickname) {
        return new UserResult(
            user.getId(),
            user.getEmail(),
            nickname
        );
    }
    
    /**
     * CreateUserCommand → 암호화된 패스워드를 가진 CreateUserCommand 변환
     * TTODO 규칙: DTO 간 변환은 무조건 매퍼로
     */
    default CreateUserCommand toEncryptedCommand(CreateUserCommand command, String encodedPassword) {
        return new CreateUserCommand(
            command.email(),
            encodedPassword,
            command.nickname(),
            command.introduction()
        );
    }
}