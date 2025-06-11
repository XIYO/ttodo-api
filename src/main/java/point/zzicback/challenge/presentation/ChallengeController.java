package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeService;
import point.zzicback.challenge.application.dto.command.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.presentation.dto.CreateChallengeResponse;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.util.List;

@Tag(name = "챌린지", description = "챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
@Transactional
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final MemberService memberService;

    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 생성 성공")
    @PostMapping
    public CreateChallengeResponse createChallenge(@Valid @RequestBody CreateChallengeCommand command) {
        Long challengeId = challengeService.createChallenge(command);
        return CreateChallengeResponse.of(challengeId);
    }

    @Operation(summary = "모든 챌린지 조회", description = "등록된 모든 챌린지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    @GetMapping
    @Transactional(readOnly = true)
    public List<ChallengeDto> getChallenges() {
        return challengeService.getChallenges();
    }

    @Operation(summary = "사용자별 챌린지 조회", description = "사용자의 참여 여부를 포함한 챌린지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 챌린지 목록 조회 성공")
    @GetMapping("/by-member")
    @Transactional(readOnly = true)
    public List<ChallengeJoinedDto> getChallengesByMember(@AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        return challengeService.getChallengesByMember(member);
    }

    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @GetMapping("/{challengeId}")
    @Transactional(readOnly = true)
    public ChallengeDto getChallenge(@PathVariable Long challengeId) {
        return challengeService.getChallenge(challengeId);
    }

    @Operation(summary = "챌린지 수정", description = "기존 챌린지 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 수정 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @PatchMapping("/{challengeId}")
    public void updateChallenge(@PathVariable Long challengeId, @RequestBody UpdateChallengeCommand command) {
        challengeService.partialUpdateChallenge(challengeId, command);
    }

    @Operation(summary = "챌린지 삭제", description = "챌린지를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 삭제 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @DeleteMapping("/{challengeId}")
    public void deleteChallenge(@PathVariable Long challengeId) {
        challengeService.deleteChallenge(challengeId);
    }

    @Operation(summary = "모든 챌린지와 참여자 조회", description = "모든 챌린지와 각 챌린지별 참여자 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 및 참여자 목록 조회 성공")
    @GetMapping("/with-participants")
    @Transactional(readOnly = true)
    public List<ChallengeDetailDto> getAllChallengesWithParticipants() {
        return challengeService.getAllChallengesWithParticipants();
    }
}
