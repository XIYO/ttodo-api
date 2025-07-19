package point.ttodoApi.challenge.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/challenges/{challengeId}/leaders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ChallengeLeader", description = "챌린지 리더 관리 API")
public class ChallengeLeaderController {
    
    private final ChallengeLeaderService leaderService;
    
    @PostMapping("/appoint")
    @Operation(summary = "리더 임명", description = "챌린지 참여자를 리더로 임명합니다. (챌린지 생성자만 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 임명 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 리더, 참여자가 아님, 최대 인원 초과 등)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<ChallengeLeaderResponse>> appointLeader(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Valid @RequestBody LeaderAppointRequest request,
            @Parameter(description = "임명자 ID") @RequestParam UUID appointedBy) {
        
        ChallengeLeader leader = leaderService.appointLeader(
            challengeId, 
            request.getMemberId(), 
            appointedBy
        );
        
        ChallengeLeaderResponse response = ChallengeLeaderResponse.from(leader);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(response));
    }
    
    @DeleteMapping("/{memberId}")
    @Operation(summary = "리더 해제", description = "리더를 해제합니다. (챌린지 생성자만 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 해제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (리더가 아님)"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<Void>> removeLeader(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "해제할 멤버 ID") @PathVariable UUID memberId,
            @Parameter(description = "해제자 ID") @RequestParam UUID removedBy,
            @Valid @RequestBody(required = false) LeaderRemoveRequest request) {
        
        String reason = request != null ? request.getReason() : null;
        leaderService.removeLeader(challengeId, memberId, removedBy, reason);
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(null));
    }
    
    @PostMapping("/resign")
    @Operation(summary = "리더 자진 사퇴", description = "리더가 스스로 사퇴합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 사퇴 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (리더가 아님)"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<Void>> resignLeader(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "사퇴할 멤버 ID") @RequestParam UUID memberId,
            @Valid @RequestBody(required = false) LeaderRemoveRequest request) {
        
        String reason = request != null ? request.getReason() : null;
        leaderService.resignLeader(challengeId, memberId, reason);
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(null));
    }
    
    @GetMapping
    @Operation(summary = "챌린지 리더 목록 조회", description = "챌린지의 활성 리더 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 참여자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<List<ChallengeLeaderResponse>>> getChallengeLeaders(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "요청자 ID") @RequestParam UUID requesterId) {
        
        List<ChallengeLeader> leaders = leaderService.getChallengeLeaders(challengeId, requesterId);
        List<ChallengeLeaderResponse> responses = leaders.stream()
            .map(ChallengeLeaderResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(responses));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "리더 통계 조회", description = "챌린지의 리더 통계를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 통계 조회 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<LeaderStatisticsResponse>> getLeaderStatistics(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
        
        ChallengeLeaderService.LeaderStatistics statistics = 
            leaderService.getLeaderStatistics(challengeId);
        
        LeaderStatisticsResponse response = LeaderStatisticsResponse.from(statistics);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(response));
    }
    
    @GetMapping("/history")
    @Operation(summary = "리더 기록 조회", description = "챌린지의 모든 리더 기록을 조회합니다. (제거된 리더 포함, 챌린지 생성자만 가능)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 기록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (챌린지 생성자가 아님)")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<List<ChallengeLeaderResponse>>> getChallengeLeaderHistory(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "요청자 ID") @RequestParam UUID requesterId) {
        
        List<ChallengeLeader> leaders = leaderService.getChallengeLeaderHistory(challengeId, requesterId);
        List<ChallengeLeaderResponse> responses = leaders.stream()
            .map(ChallengeLeaderResponse::from)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(responses));
    }
    
    @GetMapping("/{memberId}/role")
    @Operation(summary = "멤버 역할 조회", description = "특정 멤버의 챌린지 내 역할을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "역할 조회 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지 또는 멤버를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<ChallengeRole>> getMemberRole(
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        ChallengeRole role = leaderService.getMemberRole(challengeId, memberId);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(role));
    }
}

@RestController
@RequestMapping("/api/members/{memberId}/leader-challenges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MemberLeaderChallenge", description = "멤버 리더 챌린지 관리 API")
class MemberLeaderChallengeController {
    
    private final ChallengeLeaderService leaderService;
    
    @GetMapping
    @Operation(summary = "리더인 챌린지 목록", description = "멤버가 리더인 챌린지 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리더 챌린지 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<List<String>>> getMemberLeaderChallenges(
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId) {
        
        // 나중에 ChallengeResponse DTO를 만들어서 개선 필요
        List<Challenge> challenges = leaderService.getMemberLeaderChallenges(memberId);
        List<String> challengeTitles = challenges.stream()
            .map(Challenge::getTitle)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(challengeTitles));
    }
    
    @GetMapping("/{challengeId}/can-manage")
    @Operation(summary = "참여자 관리 권한 확인", description = "멤버가 특정 챌린지에서 참여자 관리 권한이 있는지 확인합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "권한 확인 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지 또는 멤버를 찾을 수 없음")
    })
    public ResponseEntity<point.ttodoApi.common.dto.ApiResponse<Boolean>> canManageParticipants(
            @Parameter(description = "멤버 ID") @PathVariable UUID memberId,
            @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
        
        boolean canManage = leaderService.canManageParticipants(challengeId, memberId);
        return ResponseEntity.ok(point.ttodoApi.common.dto.ApiResponse.success(canManage));
    }
}