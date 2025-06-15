package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeService;
import point.zzicback.challenge.application.ChallengeParticipationService;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.ChallengeDto;
import point.zzicback.challenge.application.dto.result.ChallengeListDto;
import point.zzicback.challenge.application.dto.result.ChallengeDetailDto;
import point.zzicback.challenge.application.dto.result.ParticipantDto;
import point.zzicback.challenge.presentation.dto.CreateChallengeRequest;
import point.zzicback.challenge.presentation.dto.CreateChallengeResponse;
import point.zzicback.challenge.presentation.dto.ParticipantResponse;
import point.zzicback.challenge.presentation.dto.UpdateChallengeRequest;
import point.zzicback.challenge.presentation.dto.UpdateChallengeTodoRequest;
import point.zzicback.challenge.presentation.dto.ChallengeTodoResponse;
import java.util.List;
import point.zzicback.challenge.presentation.mapper.ChallengePresentationMapper;
import point.zzicback.member.application.MemberService;
import point.zzicback.challenge.application.ChallengeTodoService;
import point.zzicback.challenge.presentation.mapper.ChallengeTodoPresentationMapper;
import point.zzicback.member.domain.Member;
import java.time.LocalDate;

@Tag(name = "챌린지", description = "챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final ChallengeParticipationService participationService;
    private final MemberService memberService;
    private final ChallengePresentationMapper challengePresentationMapper;
    private final ChallengeTodoService todoService;
    private final ChallengeTodoPresentationMapper todoMapper;

    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "챌린지 생성 성공")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateChallengeResponse createChallenge(
            @Valid @RequestBody CreateChallengeRequest request) {
        CreateChallengeCommand command = challengePresentationMapper.toCommand(request);
        Long challengeId = challengeService.createChallenge(command);
        return CreateChallengeResponse.of(challengeId);
    }

    @Operation(summary = "모든 챌린지 조회", description = "등록된 모든 챌린지를 조회합니다. 인증된 사용자의 경우 참여 여부가 포함되며, 성공률은 목록에서 제공되지 않습니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    @Operation(summary = "챌린지 목록 조회", description = "챌린지 리스트를 페이징 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    @GetMapping
    public Page<ChallengeResponse> getChallenges(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean join) {
        Pageable pageable = createPageable(page, size, sort);
        Page<ChallengeListDto> pageDto;
        if (principal != null) {
            Member member = memberService.findVerifiedMember(principal.id());
            pageDto = challengeService.searchChallengesWithFilter(member, search, sort, join, pageable);
        } else {
            pageDto = challengeService.searchChallenges(search, sort, pageable);
        }
        return pageDto.map(challengePresentationMapper::toResponse);
    }

    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다. 인증된 사용자의 경우 참여 여부와 성공률(챌린지 투두 완료율)이 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @GetMapping("/{challengeId}")
    @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공")
    public ChallengeDetailResponse getChallenge(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        ChallengeDetailResponse response;
        if (principal != null) {
            Member member = memberService.findVerifiedMember(principal.id());
            var dto = challengeService.getChallengeWithParticipation(challengeId, member);
            response = challengePresentationMapper.toResponse(dto);
        } else {
            var dto = challengePresentationMapper.toDto(
                    challengeService.getChallenge(challengeId)
            );
            response = challengePresentationMapper.toResponse(dto);
        }
        return response;
    }

    @Operation(summary = "챌린지 수정", description = "기존 챌린지 정보를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "챌린지 수정 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @PatchMapping("/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateChallenge(
            @PathVariable Long challengeId,
            @Valid @RequestBody UpdateChallengeRequest request) {
        UpdateChallengeCommand command = challengePresentationMapper.toCommand(request);
        challengeService.partialUpdateChallenge(challengeId, command);
    }

    @Operation(summary = "챌린지 삭제", description = "챌린지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "챌린지 삭제 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @DeleteMapping("/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallenge(@PathVariable Long challengeId) {
        challengeService.deleteChallenge(challengeId);
    }

    @Operation(summary = "챌린지 참여자 목록 조회", description = "특정 챌린지의 참여자 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "참여자 목록 조회 성공")
    @GetMapping("/{challengeId}/participants")
    public Page<ParticipantResponse> getParticipants(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ParticipantDto> pageDto = participationService.getParticipants(challengeId, pageable);
        return pageDto.map(challengePresentationMapper::toResponse);
    }

    @Operation(summary = "챌린지 참여", description = "특정 챌린지에 참여합니다.")
    @ApiResponse(responseCode = "201", description = "챌린지 참여 성공")
    @PostMapping("/{challengeId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipantResponse joinParticipant(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        var participation = participationService.joinChallenge(challengeId, member);
        var dto = challengePresentationMapper.toParticipantDto(participation);
        return challengePresentationMapper.toResponse(dto);
    }

    @Operation(summary = "챌린지 탈퇴", description = "특정 챌린지에서 탈퇴합니다.")
    @ApiResponse(responseCode = "204", description = "챌린지 탈퇴 성공")
    @DeleteMapping("/{challengeId}/participants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveParticipant(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        participationService.leaveChallenge(challengeId, member);
    }

    @Operation(summary = "챌린지 투두 목록 조회", description = "특정 챌린지의 투두 내역을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 투두 목록 조회 성공")
    @GetMapping("/{challengeId}/todos")
    public Page<ChallengeTodoResponse> getChallengeTodos(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        Member member = memberService.findVerifiedMember(principal.id());
        Pageable pageable = createPageable(page, size, sort);
        return todoService.getAllChallengeTodos(member, pageable)
                .map(todoMapper::toResponse);
    }

    @Operation(summary = "챌린지 투두 생성/완료 처리", description = "특정 챌린지 투두를 생성하거나 완료 상태로 변경합니다.")
    @ApiResponse(responseCode = "201", description = "챌린지 투두 생성/완료 성공")
    @PostMapping("/{challengeId}/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeTodoResponse completeChallengeTodo(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        todoService.completeChallenge(challengeId, member, LocalDate.now());
        // 재조회하여 응답 DTO 반환
        return getChallengeTodos(challengeId, principal, 0, 1, "id,desc").getContent().get(0);
    }

    @Operation(summary = "챌린지 투두 상태 수정", description = "특정 챌린지 투두의 완료 여부를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "챌린지 투두 수정 성공")
    @PatchMapping("/{challengeId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateChallengeTodo(
            @PathVariable Long challengeId,
            @PathVariable Long todoId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody UpdateChallengeTodoRequest request) {
        Member member = memberService.findVerifiedMember(principal.id());
        if (Boolean.TRUE.equals(request.done())) {
            todoService.completeChallenge(challengeId, member, LocalDate.now());
        } else {
            todoService.cancelCompleteChallenge(challengeId, member, LocalDate.now());
        }
    }

    @Operation(summary = "챌린지 투두 삭제", description = "특정 챌린지 투두를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "챌린지 투두 삭제 성공")
    @DeleteMapping("/{challengeId}/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChallengeTodo(
            @PathVariable Long challengeId,
            @PathVariable Long todoId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        todoService.cancelCompleteChallenge(challengeId, member, LocalDate.now());
    }
    private Pageable createPageable(int page, int size, String sort) {
        return switch (sort) {
            case "latest" -> PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
            case "popular" -> PageRequest.of(page, size); // 서비스 계층에서 참여자 수로 정렬된 쿼리를 사용
            default -> {
                String[] sortParams = sort.split(",");
                String property = sortParams[0];
                String direction = sortParams.length > 1 ? sortParams[1] : "desc";
                yield PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), property));
            }
        };
    }
}
