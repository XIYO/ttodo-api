package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeTodoService;
import point.zzicback.challenge.presentation.dto.ChallengeTodoResponse;
import point.zzicback.challenge.presentation.mapper.ChallengeTodoPresentationMapper;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;

@Tag(name = "챌린지 투두", description = "챌린지 투두 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenge-todos")
public class ChallengeTodoController {
    
    private final ChallengeTodoService challengeTodoService;
    private final MemberService memberService;
    private final ChallengeTodoPresentationMapper challengeTodoPresentationMapper;

    @Operation(summary = "현재 기간 챌린지 투두 조회", description = "현재 기간 내 사용자의 모든 챌린지 투두를 조회합니다. 기간이 지난 투두는 제외됩니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 투두 목록 조회 성공")
    @GetMapping
    public Page<ChallengeTodoResponse> getAllChallengeTodos(@AuthenticationPrincipal MemberPrincipal principal,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(defaultValue = "id,desc") String sort) {
        Member member = memberService.findVerifiedMember(principal.id());
        Pageable pageable = createPageable(page, size, sort);
        return challengeTodoService.getAllChallengeTodos(member, pageable)
                .map(challengeTodoPresentationMapper::toResponse);
    }

    @Operation(summary = "현재 기간 미완료 투두 조회", description = "현재 기간 내 사용자의 미완료 챌린지 투두만 조회합니다. 기간이 지난 투두는 제외됩니다.")
    @ApiResponse(responseCode = "200", description = "미완료 챌린지 투두 목록 조회 성공")
    @GetMapping("/uncompleted")
    public Page<ChallengeTodoResponse> getUncompletedChallengeTodos(@AuthenticationPrincipal MemberPrincipal principal,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(defaultValue = "id,desc") String sort) {
        Member member = memberService.findVerifiedMember(principal.id());
        Pageable pageable = createPageable(page, size, sort);
        return challengeTodoService.getUncompletedChallengeTodos(member, pageable)
                .map(challengeTodoPresentationMapper::toResponse);
    }

    @Operation(summary = "완료된 챌린지 투두 조회", description = "사용자의 완료된 챌린지 투두만 조회합니다.")
    @ApiResponse(responseCode = "200", description = "완료된 챌린지 투두 목록 조회 성공")
    @GetMapping("/completed")
    public Page<ChallengeTodoResponse> getCompletedChallengeTodos(@AuthenticationPrincipal MemberPrincipal principal,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "id,desc") String sort) {
        Member member = memberService.findVerifiedMember(principal.id());
        Pageable pageable = createPageable(page, size, sort);
        return challengeTodoService.getCompletedChallengeTodos(member, pageable)
                .map(challengeTodoPresentationMapper::toResponse);
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

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        String direction = sortParams.length > 1 ? sortParams[1] : "desc";
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), property));
    }
}
