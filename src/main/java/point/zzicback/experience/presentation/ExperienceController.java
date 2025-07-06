package point.zzicback.experience.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import point.zzicback.experience.application.ExperienceService;
import point.zzicback.experience.presentation.dto.response.MemberLevelResponse;
import point.zzicback.experience.presentation.mapper.ExperiencePresentationMapper;

import java.util.UUID;

@Tag(name = "회원 경험치 및 레벨 시스템", description = "회원의 활동 기반 경험치 축적 및 레벨 상승 시스템 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class ExperienceController {

    private final ExperienceService experienceService;
    private final ExperiencePresentationMapper mapper;

    @Operation(summary = "회원 레벨/경험치 조회", description = "특정 회원의 레벨과 경험치를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "레벨/경험치 조회 성공")
    @GetMapping("/{memberId}/experience")
    public MemberLevelResponse getMemberLevel(@PathVariable UUID memberId) {
        var result = experienceService.getMemberLevel(memberId);
        return mapper.toResponse(result);
    }
}
