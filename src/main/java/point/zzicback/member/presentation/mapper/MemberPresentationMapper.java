package point.zzicback.member.presentation.mapper;

import org.mapstruct.Mapper;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.application.dto.result.MemberDto;
import point.zzicback.member.domain.Member;
import point.zzicback.member.presentation.dto.request.UpdateMemberRequest;
import point.zzicback.member.presentation.dto.response.MemberResponse;

import java.util.UUID;
import org.mapstruct.Mapping;

/**
 * Presentation 레이어 요청/응답과 Application/Domain DTO 간 변환을 담당하는 Mapper
 */
@Mapper(componentModel = "spring")
public interface MemberPresentationMapper {

    /** Presentation 요청 DTO -> Application Command 변환 */
    @Mapping(target = "memberId", source = "memberId")
    @Mapping(target = "nickname", source = "request.nickname")
    UpdateMemberCommand toCommand(UUID memberId, UpdateMemberRequest request);

    /** Domain Member -> Application MemberDto 변환 */
    MemberDto toDto(Member member);

    /** Application MemberDto -> Presentation Response DTO 변환 */
    MemberResponse toResponse(MemberDto dto);
}