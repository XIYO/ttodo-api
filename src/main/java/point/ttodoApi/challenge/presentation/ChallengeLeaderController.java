package point.ttodoApi.challenge.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.ttodoApi.challenge.application.ChallengeLeaderService;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.dto.*;

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
    
    @PostMapping(value = "/appoint", consumes = { org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE, org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "리더 임명", description = "챌린지 참여자를 리더로 임명합니다. 챌린지 생성자만 이 작업을 수행할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 임명 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 리더이거나 참여자가 아닌 경우, 최대 리더 수 초과)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<ChallengeLeaderResponse>> appointLeader(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Valid LeaderAppointRequest request,
            @AuthenticationPrincipal point.ttodoApi.auth.domain.MemberPrincipal principal) {
        
        ChallengeLeader leader = leaderService.appointLeader(
            challengeId, 
            request.getMemberId(), 
            principal.id()
        );
        
        ChallengeLeaderResponse response = ChallengeLeaderResponse.from(leader);
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(response));
    }
    
    @DeleteMapping("/{memberId}")
    @Operation(summary = "리더 해제", description = "리더 권한을 해제합니다. 챌린지 생성자만 이 작업을 수행할 수 있습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 해제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (해당 멤버가 리더가 아님)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<Void>> removeLeader(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "해제할 멤버 ID") @PathVariable UUID memberId,
            @AuthenticationPrincipal point.ttodoApi.auth.domain.MemberPrincipal principal,
            @Valid LeaderRemoveRequest request) {
        
        String reason = request != null ? request.getReason() : null;
        leaderService.removeLeader(challengeId, memberId, principal.id(), reason);
        
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(null));
    }
    
    @PostMapping(value = "/resign", consumes = { org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE, org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "리더 자진 사퇴", description = "리더가 스스로 리더 권한을 포기합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 사퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (해당 멤버가 리더가 아님)"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<Void>> resignLeader(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @AuthenticationPrincipal point.ttodoApi.auth.domain.MemberPrincipal principal,
            @Valid LeaderRemoveRequest request) {
        
        String reason = request != null ? request.getReason() : null;
        leaderService.resignLeader(challengeId, principal.id(), reason);
        
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(null));
    }
    
    @GetMapping
    @Operation(summary = "챌린지 리더 목록 조회", description = "현재 활동 중인 챌린지 리더 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 참여자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<List<ChallengeLeaderResponse>>> getChallengeLeaders(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @AuthenticationPrincipal point.ttodoApi.auth.domain.MemberPrincipal principal) {
        
        List<ChallengeLeader> leaders = leaderService.getChallengeLeaders(challengeId, principal.id());
        List<ChallengeLeaderResponse> responses = leaders.stream()
            .map(ChallengeLeaderResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(responses));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "리더 통계 조회", description = "챌린지 리더 관련 통계 정보를 조회합니다. 전체 리더 수, 활동 리더 수 등의 정보를 제공합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 통계 조회 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<LeaderStatisticsResponse>> getLeaderStatistics(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
        
        ChallengeLeaderService.LeaderStatistics statistics = 
            leaderService.getLeaderStatistics(challengeId);
        
        LeaderStatisticsResponse response = LeaderStatisticsResponse.from(statistics);
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(response));
    }
    
    @GetMapping("/history")
    @Operation(summary = "리더 기록 조회", description = "챌린지의 모든 리더 기록을 조회합니다. 해제되거나 사퇴한 리더 정보도 포함됩니다. 챌린지 생성자만 조회 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 기록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<List<ChallengeLeaderResponse>>> getChallengeLeaderHistory(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @AuthenticationPrincipal point.ttodoApi.auth.domain.MemberPrincipal principal) {
        
        List<ChallengeLeader> leaders = leaderService.getChallengeLeaderHistory(challengeId, principal.id());
        List<ChallengeLeaderResponse> responses = leaders.stream()
            .map(ChallengeLeaderResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(responses));
    }
    
    @GetMapping("/{memberId}/role")
    @Operation(summary = "멤버 역할 조회", description = "특정 멤버의 챌린지 내 역할을 조회합니다. 일반 참가자, 리더, 생성자 등의 역할 정보를 반환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "역할 조회 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지 또는 멤버를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<ChallengeRole>> getMemberRole(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        ChallengeRole role = leaderService.getMemberRole(challengeId, memberId);
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(role));
    }
}

@RestController
@RequestMapping("/members/{memberId}/leader-challenges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "멤버별 리더 챌린지 조회", description = "특정 멤버가 리더로 활동 중인 챌린지 목록과 관련 정보를 조회합니다.")
class MemberLeaderChallengeController {
    
    private final ChallengeLeaderService leaderService;
    
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("#memberId == authentication.principal.id")
    @Operation(summary = "리더로 활동 중인 챌린지 목록 조회", description = "특정 멤버가 리더 역할을 맡고 있는 모든 챌린지 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 챌린지 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.shared.dto.ApiResponse<List<String>>> getMemberLeaderChallenges(
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        // 나중에 ChallengeResponse DTO를 만들어서 개선 필요
        List<Challenge> challenges = leaderService.getMemberLeaderChallenges(memberId);
        List<String> challengeTitles = challenges.stream()
            .map(Challenge::getTitle)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.shared.dto.ApiResponse.success(challengeTitles));
    }
}
