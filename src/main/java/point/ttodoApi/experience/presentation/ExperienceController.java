package point.ttodoApi.experience.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.experience.application.ExperienceService;
import point.ttodoApi.experience.presentation.dto.response.UserLevelResponse;
import point.ttodoApi.experience.presentation.mapper.ExperiencePresentationMapper;

import java.util.UUID;

@Tag(name = "경험치/레벨(Experience) 시스템", description = "할 일 완료, 챌린지 참여 등 사용자 활동에 따른 경험치 축적 및 레벨 시스템을 관리합니다. 경험치가 쉼으면 레벨이 상승하며, 이를 통해 사용자의 성취감을 높입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class ExperienceController {

  private final ExperienceService experienceService;
  private final ExperiencePresentationMapper mapper;

  @Operation(
          summary = "회원 레벨/경험치 조회",
          description = "특정 회원의 현재 레벨, 보유 경험치, 다음 레벨까지 필요한 경험치 등의 정보를 조회합니다.\n\n" +
                  "레벨 시스템:\n" +
                  "- 레벨 1: 0 XP\n" +
                  "- 레벨 2: 100 XP\n" +
                  "- 레벨 3: 300 XP\n" +
                  "- ... (레벨이 오를수록 필요 경험치 증가)\n\n" +
                  "경험치 획득 방법:\n" +
                  "- 할 일 완료: +10 XP\n" +
                  "- 챌린지 할 일 완료: +15 XP\n" +
                  "- 일일 목표 달성: +20 XP"
  )
  @ApiResponse(responseCode = "200", description = "레벨/경험치 조회 성공")
  @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
  @GetMapping("/{userId}/experience")
  @PreAuthorize("hasRole('USER')")
  public UserLevelResponse getUserLevel(@PathVariable UUID userId) {
    var result = experienceService.getUserLevel(userId);
    return mapper.toResponse(result);
  }
}
