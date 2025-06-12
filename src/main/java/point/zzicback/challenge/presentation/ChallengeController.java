package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeService;
import point.zzicback.challenge.application.dto.command.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.presentation.dto.CreateChallengeResponse;
import point.zzicback.challenge.presentation.mapper.ChallengePresentationMapper;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

@Tag(name = "챌린지", description = "챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final ChallengePresentationMapper challengePresentationMapper;

    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 생성 성공")
    @PostMapping
    public CreateChallengeResponse createChallenge(@Valid @RequestBody CreateChallengeCommand command) {
        Long challengeId = challengeService.createChallenge(command);
        return CreateChallengeResponse.of(challengeId);
    }

    @Operation(summary = "모든 챌린지 조회", description = "등록된 모든 챌린지를 조회합니다. 인증된 사용자의 경우 참여 여부가 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    @GetMapping
    public Page<ChallengeDto> getChallenges(@AuthenticationPrincipal MemberPrincipal principal,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "id,desc") String sort) {
        Pageable pageable = createPageable(page, size, sort);
        
        if (principal != null) {
            Member member = memberService.findVerifiedMember(principal.id());
            return challengeService.getChallengesWithParticipation(member, pageable);
        } else {
            return challengeService.getChallenges(pageable).map(challengePresentationMapper::toDto);
        }
    }

    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @GetMapping("/{challengeId}")
    public ChallengeDto getChallenge(@PathVariable Long challengeId) {
        Challenge challenge = challengeService.getChallenge(challengeId);
        return challengePresentationMapper.toDto(challenge);
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
    public Page<ChallengeDetailDto> getAllChallengesWithParticipants(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(defaultValue = "id,desc") String sort) {
        Pageable pageable = createPageable(page, size, sort);
        return challengeService.getAllChallengesWithParticipants(pageable).map(challengePresentationMapper::toDetailDto);
    }

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        String direction = sortParams.length > 1 ? sortParams[1] : "desc";
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), property));
    }
}
