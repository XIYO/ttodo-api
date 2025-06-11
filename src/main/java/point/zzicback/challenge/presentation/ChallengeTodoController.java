package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeTodoService;
import point.zzicback.challenge.application.dto.result.ChallengeTodoDto;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "챌린지 투두", description = "챌린지 투두 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge-todos")
@Transactional
public class ChallengeTodoController {
    
    private final ChallengeTodoService challengeTodoService;
    private final MemberService memberService;

    @Operation(summary = "모든 챌린지 투두 조회", description = "사용자의 모든 챌린지 투두를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 투두 목록 조회 성공")
    @GetMapping
    public List<ChallengeTodoDto> getAllChallengeTodos(@AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        return challengeTodoService.getAllChallengeTodos(member);
    }

    @Operation(summary = "미완료 챌린지 투두 조회", description = "사용자의 미완료 챌린지 투두만 조회합니다.")
    @ApiResponse(responseCode = "200", description = "미완료 챌린지 투두 목록 조회 성공")
    @GetMapping("/uncompleted")
    public List<ChallengeTodoDto> getUncompletedChallengeTodos(@AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        return challengeTodoService.getUncompletedChallengeTodos(member);
    }

    @Operation(summary = "완료된 챌린지 투두 조회", description = "사용자의 완료된 챌린지 투두만 조회합니다.")
    @ApiResponse(responseCode = "200", description = "완료된 챌린지 투두 목록 조회 성공")
    @GetMapping("/completed")
    public List<ChallengeTodoDto> getCompletedChallengeTodos(@AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        return challengeTodoService.getCompletedChallengeTodos(member);
    }

    @Operation(summary = "챌린지 완료 처리", description = "특정 챌린지를 완료 처리합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 완료 처리 성공")
    @ApiResponse(responseCode = "404", description = "챌린지 참여 정보를 찾을 수 없음")
    @PostMapping("/{challengeId}/complete")
    public void completeChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        challengeTodoService.completeChallenge(challengeId, member, LocalDate.now());
    }

    @Operation(summary = "챌린지 완료 취소", description = "완료된 챌린지를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 완료 취소 성공")
    @ApiResponse(responseCode = "404", description = "챌린지 투두를 찾을 수 없음")
    @DeleteMapping("/{challengeId}/complete")
    public void cancelCompleteChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        challengeTodoService.cancelCompleteChallenge(challengeId, member, LocalDate.now());
    }
}
