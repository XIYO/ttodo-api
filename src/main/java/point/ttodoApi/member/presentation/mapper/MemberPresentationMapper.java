package point.ttodoApi.member.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.common.config.MapStructConfig;
import point.ttodoApi.member.application.dto.command.UpdateMemberCommand;
import point.ttodoApi.member.application.dto.result.MemberResult;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.presentation.dto.request.UpdateMemberRequest;
import point.ttodoApi.member.presentation.dto.response.MemberResponse;
import point.ttodoApi.profile.domain.Profile;

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
    @Mapping(source = "profile.introduction", target = "introduction")
    @Mapping(source = "profile.locale", target = "locale")
    @Mapping(source = "profile.timeZone", target = "timeZone")
    @Mapping(source = "profile.theme", target = "theme")
    @Mapping(target = "profileImageUrl", expression = "java(profile != null ? profile.getImageUrl() : null)")
    MemberResponse toResponse(MemberResult dto, Profile profile);
    
    /** Simple version without profile (for list views) */
    @Mapping(target = "introduction", constant = "")
    @Mapping(target = "locale", constant = "ko-KR")
    @Mapping(target = "timeZone", constant = "Asia/Seoul")
    @Mapping(target = "theme", constant = "LIGHT")
    @Mapping(target = "profileImageUrl", expression = "java(null)")
    MemberResponse toResponse(MemberResult dto);
}