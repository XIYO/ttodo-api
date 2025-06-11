package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeParticipationService;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

@Tag(name = "챌린지 참여", description = "챌린지 참여 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge-participations")
@Transactional
public class ChallengeParticipationController {
    
    private final ChallengeParticipationService participationService;
    private final MemberService memberService;

    @Operation(summary = "챌린지 참여", description = "특정 챌린지에 참여합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 참여 성공")
    @ApiResponse(responseCode = "400", description = "이미 참여중인 챌린지")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @PostMapping("/{challengeId}/join")
    public void joinChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        participationService.joinChallenge(challengeId, member);
    }

    @Operation(summary = "챌린지 탈퇴", description = "참여중인 챌린지에서 탈퇴합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 탈퇴 성공")
    @ApiResponse(responseCode = "400", description = "참여하지 않은 챌린지")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @DeleteMapping("/{challengeId}/leave")
    public void leaveChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        participationService.leaveChallenge(challengeId, member);
    }
}
