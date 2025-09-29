package point.ttodoApi.challenge.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import point.ttodoApi.challenge.application.*;
import point.ttodoApi.challenge.application.command.*;
import point.ttodoApi.challenge.application.result.ChallengeResult;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.presentation.dto.request.*;
import point.ttodoApi.challenge.presentation.dto.response.*;
import point.ttodoApi.challenge.presentation.mapper.ChallengePresentationMapper;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.shared.validation.*;

/**
 * 챌린지 기본 CRUD API 컨트롤러
 */
@Tag(name = "챌린지(Challenge) 관리", description = "사용자들이 함께 목표를 달성하고 경쟁하는 챌린지 기능을 관리합니다. 챌린지 생성, 조회, 수정, 삭제 및 초대 링크 관리 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {

  private final ChallengeService challengeService;
  private final ChallengeSearchService challengeSearchService;
  private final UserService UserService;
  private final ChallengePresentationMapper challengePresentationMapper;

  @Operation(
          summary = "새 챌린지 생성",
          description = "새로운 챌린지를 생성합니다. 챌린지는 여러 사용자가 함께 할 일을 수행하고 목표를 달성하는 그룹 활동입니다.\n\n" +
                  "필수 필드:\n" +
                  "- title: 챌린지 제목\n" +
                  "- description: 챌린지 설명\n" +
                  "- startDate: 시작일 (YYYY-MM-DD)\n" +
                  "- endDate: 종료일 (YYYY-MM-DD)\n\n" +
                  "선택 필드:\n" +
                  "- maxParticipants: 최대 참가 인원\n" +
                  "- isPublic: 공개 여부",
          requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  content = {
                          @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = CreateChallengeRequest.class)),
                          @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = CreateChallengeRequest.class))
                  }
          )
  )
  @ApiResponse(responseCode = "201", description = "챌린지 생성 성공")
  @ApiResponse(responseCode = "400", description = "입력값 검증 실패 (종료일이 시작일보다 빠름, 필수값 누락 등)")
  @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
  @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('USER')")
  public ChallengeResponse createChallenge(
          @Valid CreateChallengeRequest request,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    point.ttodoApi.user.domain.User domainUser = UserService.findVerifiedUser(UUID.fromString(user.getUsername()));
    CreateChallengeCommand command = challengePresentationMapper.toCommand(request, domainUser.getId());
    Long challengeId = challengeService.createChallenge(command);
    ChallengeResult result = challengeService.getChallengeDetailForPublic(challengeId);
    return challengePresentationMapper.toChallengeResponse(result);
  }

  @Operation(
          summary = "챌린지 목록 조회/검색",
          description = "챌린지 목록을 페이지네이션과 함께 조회하거나 검색합니다. 다양한 필터를 통해 원하는 챌린지를 찾을 수 있습니다.\n\n" +
                  "검색 파라미터:\n" +
                  "- titleKeyword: 제목 키워드\n" +
                  "- descriptionKeyword: 설명 키워드\n" +
                  "- visibility: 공개 여부 (PUBLIC, PRIVATE, FRIENDS_ONLY)\n" +
                  "- periodType: 기간 타입 (DAILY, WEEKLY, MONTHLY, CUSTOM)\n" +
                  "- ongoingOnly: 진행 중인 챌린지만\n" +
                  "- joinableOnly: 참여 가능한 챌린지만\n" +
                  "\n페이지네이션 파라미터:\n" +
                  "- page: 페이지 번호 (0부터 시작, 기본값: 0)\n" +
                  "- size: 페이지 크기 (1-100, 기본값: 20)\n" +
                  "- sort: 정렬 기준 (id, title, createdAt, startDate, endDate, participantCount)"
  )
  @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
  @GetMapping
  @PreAuthorize("hasRole('USER')")
  @ValidPageable(sortFields = SortFieldsProvider.CHALLENGE)
  public Page<ChallengeResponse> getChallenges(
          @Parameter(description = "검색 조건") @ModelAttribute ChallengeSearchRequest request,
          @Parameter(description = "페이징 및 정렬 정보")
          @PageableDefault(page = 0, size = 20, sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {

    request.validate();
    Page<Challenge> challengePage = challengeSearchService.searchChallenges(request, pageable);
    return challengePage.map(challengePresentationMapper::toChallengeSummaryResponse);
  }

  @Operation(
          summary = "챌린지 상세 조회",
          description = "특정 챌린지의 상세 정보를 조회합니다. 챌린지 제목, 설명, 기간, 참가자 수, 생성자 정보 등을 포함합니다."
  )
  @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공")
  @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
  @GetMapping("/{challengeId}")
  @PreAuthorize("hasRole('USER')")
  public ChallengeDetailResponse getChallenge(@PathVariable Long challengeId) {
    ChallengeResult result = challengeService.getChallengeDetailForPublic(challengeId);
    return challengePresentationMapper.toResponse(result);
  }

  @Operation(
          summary = "챌린지 정보 수정",
          description = "챌린지 정보를 부분 수정합니다. 챌린지 생성자만 수정할 수 있습니다.\n\n" +
                  "수정 가능한 필드:\n" +
                  "- title: 챌린지 제목\n" +
                  "- description: 챌린지 설명\n" +
                  "- endDate: 종료일 (연장만 가능)\n" +
                  "- maxParticipants: 최대 참가 인원\n" +
                  "- isPublic: 공개 여부"
  )
  @ApiResponse(responseCode = "204", description = "챌린지 수정 성공")
  @ApiResponse(responseCode = "400", description = "잘못된 수정 요청 (종료일을 과거로 변경 등)")
  @ApiResponse(responseCode = "403", description = "챌린지 생성자가 아님")
  @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
  @PatchMapping(value = "/{challengeId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#challengeId, 'Challenge', 'WRITE')")
  public void updateChallenge(
          @PathVariable Long challengeId,
          @Valid UpdateChallengeRequest request,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
    UpdateChallengeCommand command = challengePresentationMapper.toCommand(request);
    challengeService.partialUpdateChallenge(challengeId, command);
  }

  @Operation(
          summary = "챌린지 삭제",
          description = "챌린지를 영구적으로 삭제합니다. 챌린지 생성자만 삭제할 수 있으며, 모든 참가자의 챌린지 데이터도 함께 삭제됩니다."
  )
  @ApiResponse(responseCode = "204", description = "챌린지 삭제 성공")
  @ApiResponse(responseCode = "403", description = "챌린지 생성자가 아님")
  @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
  @DeleteMapping("/{challengeId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#challengeId, 'Challenge', 'DELETE')")
  public void deleteChallenge(@PathVariable Long challengeId) {
    challengeService.deleteChallenge(challengeId);
  }

  @Operation(
          summary = "챌린지 초대 링크 조회",
          description = "챌린지에 다른 사용자를 초대하기 위한 초대 코드와 링크를 조회합니다. 챌린지 생성자만 조회할 수 있습니다. 초대 코드는 챌린지마다 고유하게 생성됩니다."
  )
  @ApiResponse(responseCode = "200", description = "초대 링크 조회 성공")
  @ApiResponse(responseCode = "403", description = "챌린지 생성자가 아님")
  @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
  @GetMapping("/{challengeId}/invite-link")
  @PreAuthorize("hasPermission(#challengeId, 'Challenge', 'READ')")
  public InviteLinkResponse getInviteLink(@PathVariable Long challengeId) {
    Challenge challenge = challengeService.getChallenge(challengeId);
    return new InviteLinkResponse(
            challenge.getInviteCode(),
            "https://ttodo.dev/challenges/invite/" + challenge.getInviteCode()
    );
  }


}
