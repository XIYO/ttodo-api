package point.zzicback.member.application.mapper;

import org.mapstruct.Mapper;
import point.zzicback.member.application.dto.response.MemberMeResponse;
import point.zzicback.member.domain.Member;

@Mapper(componentModel = "spring")
public interface MemberApplicationMapper {

    MemberMeResponse toResponse(Member member);
}
