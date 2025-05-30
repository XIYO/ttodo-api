package point.zzicback.member.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.member.application.dto.command.SignUpCommand;
import point.zzicback.member.application.dto.response.MemberMeResponse;
import point.zzicback.member.domain.Member;

@Mapper(componentModel = "spring")
public interface MemberApplicationMapper {

    @Mapping(target = "id", ignore = true)
    Member toEntity(SignUpCommand command);

    MemberMeResponse toResponse(Member member);
}
