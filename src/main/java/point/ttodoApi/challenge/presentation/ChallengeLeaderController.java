package point.ttodoApi.challenge.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.challenge.application.ChallengeLeaderService;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.presentation.dto.request.*;
import point.ttodoApi.challenge.presentation.dto.response.*;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 챌린지 리더 관리 컨트롤러
 */
@RestController
@RequestMapping("/challenges/{challengeId}/leaders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "챌린지 리더 관리", description = "챌린지 내에서 리더 역할을 담당하는 참가자들을 관리합니다. 리더 임명, 해제, 사퇴 및 리더 관련 통계를 제공합니다.")
public class ChallengeLeaderController {

  private final ChallengeLeaderService leaderService;
  private final ProfileService profileService;

  @PostMapping(consumes = {org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE, org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasPermission(#challengeId, 'Challenge', 'WRITE')")
  @Operation(summary = "리더 추가", description = "챌린지에 새로운 리더를 추가합니다. 챌린지 생성자만 이 작업을 수행할 수 있습니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "201", description = "리더 추가 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 리더이거나 참여자가 아닌 경우, 최대 리더 수 초과)"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
  })
  public ChallengeLeaderResponse addLeader(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
          @Valid LeaderAppointRequest request,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    ChallengeLeader leader = leaderService.appointLeader(
            challengeId,
            request.getUserId(),
            UUID.fromString(user.getUsername())
    );

    Profile userProfile = profileService.getProfile(leader.getUser().getId());
    return ChallengeLeaderResponse.from(leader, userProfile.getNickname());
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasPermission(#challengeId, 'Challenge', 'WRITE')")
  @Operation(summary = "리더 해제", description = "리더 권한을 해제합니다. 챌린지 생성자만 이 작업을 수행할 수 있습니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "리더 해제 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (해당 멤버가 리더가 아님)"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
  })
  public void removeLeader(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
          @Parameter(description = "해제할 멤버 ID") @PathVariable UUID userId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @Valid LeaderRemoveRequest request) {

    String reason = request != null ? request.getReason() : null;
    leaderService.removeLeader(challengeId, userId, UUID.fromString(user.getUsername()), reason);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "리더 자진 사퇴", description = "리더가 스스로 리더 권한을 포기합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "리더 사퇴 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (해당 멤버가 리더가 아님)"),
          @ApiResponse(responseCode = "403", description = "권한 없음")
  })
  public void resignLeader(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    leaderService.resignLeader(challengeId, UUID.fromString(user.getUsername()), null);
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "챌린지 리더 목록 조회", description = "현재 활동 중인 챌린지 리더 목록을 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "리더 목록 조회 성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 참여자가 아님)")
  })
  public List<ChallengeLeaderResponse> getChallengeLeaders(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    List<ChallengeLeader> leaders = leaderService.getChallengeLeaders(challengeId, UUID.fromString(user.getUsername()));
    return leaders.stream()
            .map(leader -> {
              Profile userProfile = profileService.getProfile(leader.getUser().getId());
              return ChallengeLeaderResponse.from(leader, userProfile.getNickname());
            })
            .collect(Collectors.toList());
  }

  @GetMapping("/statistics")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "리더 통계 조회", description = "챌린지 리더 관련 통계 정보를 조회합니다. 전체 리더 수, 활동 리더 수 등의 정보를 제공합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "리더 통계 조회 성공"),
          @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
  })
  public LeaderStatisticsResponse getLeaderStatistics(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {

    ChallengeLeaderService.LeaderStatistics statistics =
            leaderService.getLeaderStatistics(challengeId);

    return LeaderStatisticsResponse.from(statistics);
  }

  @GetMapping("/history")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasPermission(#challengeId, 'Challenge', 'READ')")
  @Operation(summary = "리더 기록 조회", description = "챌린지의 모든 리더 기록을 조회합니다. 해제되거나 사퇴한 리더 정보도 포함됩니다. 챌린지 생성자만 조회 가능합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "리더 기록 조회 성공"),
          @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
  })
  public List<ChallengeLeaderResponse> getChallengeLeaderHistory(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

    List<ChallengeLeader> leaders = leaderService.getChallengeLeaderHistory(challengeId, UUID.fromString(user.getUsername()));
    return leaders.stream()
            .map(leader -> {
              Profile userProfile = profileService.getProfile(leader.getUser().getId());
              return ChallengeLeaderResponse.from(leader, userProfile.getNickname());
            })
            .collect(Collectors.toList());
  }

  @GetMapping("/{userId}/role")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "멤버 역할 조회", description = "특정 멤버의 챌린지 내 역할을 조회합니다. 일반 참가자, 리더, 생성자 등의 역할 정보를 반환합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "역할 조회 성공"),
          @ApiResponse(responseCode = "404", description = "챌린지 또는 멤버를 찾을 수 없음")
  })
  public ChallengeRole getUserRole(
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
          @Parameter(description = "멤버 ID") @PathVariable UUID userId) {

    return leaderService.getUserRole(challengeId, userId);
  }
}

@RestController
@RequestMapping("/user/{userId}/leader-challenges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "멤버별 리더 챌린지 조회", description = "특정 멤버가 리더로 활동 중인 챌린지 목록과 관련 정보를 조회합니다.")
class UserLeaderChallengeController {

  private final ChallengeLeaderService leaderService;

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @org.springframework.security.access.prepost.PreAuthorize("#userId == authentication.principal.id")
  @Operation(summary = "리더로 활동 중인 챌린지 목록 조회", description = "특정 멤버가 리더 역할을 맡고 있는 모든 챌린지 목록을 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "리더 챌린지 목록 조회 성공"),
          @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
  })
  public List<String> getUserLeaderChallenges(
          @Parameter(description = "멤버 ID") @PathVariable UUID userId) {

    // 나중에 ChallengeResponse DTO를 만들어서 개선 필요
    List<Challenge> challenges = leaderService.getUserLeaderChallenges(userId);
    return challenges.stream()
            .map(Challenge::getTitle)
            .collect(Collectors.toList());
  }
}
