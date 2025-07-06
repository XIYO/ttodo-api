package point.zzicback.member.presentation.mapper;

import org.mapstruct.*;
import point.zzicback.common.config.MapStructConfig;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.application.dto.result.MemberResult;
import point.zzicback.member.domain.Member;
import point.zzicback.member.presentation.dto.request.UpdateMemberRequest;
import point.zzicback.member.presentation.dto.response.MemberResponse;
import point.zzicback.profile.domain.MemberProfile;

import java.util.UUID;

/**
 * Presentation 레이어 요청/응답과 Application/Domain DTO 간 변환을 담당하는 Mapper
 */
@Mapper(config = MapStructConfig.class)
public interface MemberPresentationMapper {

    /** Presentation 요청 DTO -> Application Command 변환 */
    UpdateMemberCommand toCommand(UUID memberId, UpdateMemberRequest request);

    /** Domain Member -> Application MemberResult 변환 */
    MemberResult toResult(Member member);

    /** Application MemberResult -> Presentation Response DTO 변환 */
    @Mapping(source = "dto.id", target = "id")
    @Mapping(source = "dto.email", target = "email")
    @Mapping(source = "dto.nickname", target = "nickname")
    @Mapping(source = "dto.introduction", target = "introduction")
    @Mapping(source = "dto.locale", target = "locale")
    @Mapping(source = "dto.timeZone", target = "timeZone")
    @Mapping(source = "profile.theme", target = "theme")
    @Mapping(target = "hasProfileImage", expression = "java(profile != null && profile.getProfileImage() != null)")
    MemberResponse toResponse(MemberResult dto, MemberProfile profile);
    
    /** Simple version without profile (for list views) */
    @Mapping(target = "theme", constant = "LIGHT")
    @Mapping(target = "hasProfileImage", constant = "false")
    MemberResponse toResponse(MemberResult dto);
}