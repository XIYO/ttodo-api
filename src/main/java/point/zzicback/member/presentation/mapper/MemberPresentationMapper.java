package point.zzicback.member.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.member.application.dto.command.SignUpCommand;
import point.zzicback.member.application.dto.command.SignInCommand;
import point.zzicback.member.presentation.dto.SignUpRequest;
import point.zzicback.member.presentation.dto.SignInRequest;
import point.zzicback.member.presentation.dto.MemberMeResponse;

@Mapper(componentModel = "spring")
public interface MemberPresentationMapper {

    SignUpCommand toCommand(SignUpRequest request);

    SignInCommand toCommand(SignInRequest request);

    MemberMeResponse toResponse(point.zzicback.member.application.dto.response.MemberMeResponse applicationResponse);
}
