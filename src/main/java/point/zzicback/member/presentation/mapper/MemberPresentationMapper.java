package point.zzicback.member.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.member.presentation.dto.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberPresentationMapper {

    MemberResponse toResponse(point.zzicback.member.application.dto.response.MemberMeResponse applicationResponse);
}
