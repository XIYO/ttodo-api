package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.*;
import point.zzicback.challenge.application.dto.command.*;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.presentation.dto.request.*;
import point.zzicback.challenge.presentation.dto.response.*;
import point.zzicback.challenge.presentation.mapper.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;

@Tag(name = "챌린지", description = "챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final ChallengeParticipationService participationService;
    private final MemberService memberService;
    private final ChallengePresentationMapper challengePresentationMapper;
    private final ChallengeTodoService todoService;
    private final ChallengeTodoPresentationMapper todoMapper;

    @Operation(
        summary = "챌린지 생성", 
        description = "새로운 챌린지를 생성합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = {
                @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = CreateChallengeRequest.class)),
                @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = CreateChallengeRequest.class))
            }
        )
    )
    @ApiResponse(responseCode = "201", description = "챌린지 생성 성공")
    @PostMapping(consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseStatus(HttpStatus.CREATED)
    public CreateChallengeResponse createChallenge(
            @Valid CreateChallengeRequest request) {
        CreateChallengeCommand command = challengePresentationMapper.toCommand(request);
        Long challengeId = challengeService.createChallenge(command);
        return CreateChallengeResponse.of(challengeId);
    }

    @Operation(summary = "모든 챌린지 조회", description = "등록된 모든 챌린지를 조회합니다. 인증된 사용자의 경우 참여 여부가 포함되며, 성공률은 목록에서 제공되지 않습니다.")
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
        Page<ChallengeListResult> pageDto;
        
        if (principal != null) {
            Member member = memberService.findVerifiedMember(principal.id());
            pageDto = challengeService.searchChallengesWithFilter(member, search, sort, join, pageable);
        } else {
            pageDto = challengeService.searchChallengesWithFilter(null, search, sort, join, pageable);
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
            var dto = challengePresentationMapper.toResult(
                    challengeService.getChallenge(challengeId)
            );
            response = challengePresentationMapper.toResponse(dto);
        }
        return response;
    }

    @Operation(
        summary = "챌린지 수정", 
        description = "기존 챌린지 정보를 수정합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = {
                @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE, schema = @Schema(implementation = UpdateChallengeRequest.class)),
                @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(implementation = UpdateChallengeRequest.class))
            }
        )
    )
    @ApiResponse(responseCode = "204", description = "챌린지 수정 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @PatchMapping(value = "/{challengeId}", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateChallenge(
            @PathVariable Long challengeId,
            @Valid UpdateChallengeRequest request) {
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
        Page<ParticipantResult> pageDto = participationService.getParticipants(challengeId, pageable);
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
        var dto = challengePresentationMapper.toParticipantResult(participation);
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

    @Operation(summary = "챌린지 투두 목록 조회", description = "사용자의 모든 챌린지 투두 내역을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 투두 목록 조회 성공")
    @GetMapping("/todos")
    public Page<ChallengeTodoResponse> getChallengeTodos(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        Member member = memberService.findVerifiedMember(principal.id());
        Pageable pageable = createPageable(page, size, sort);
        return todoService.getAllChallengeTodos(member, pageable)
                .map(todoMapper::toResponse);
    }

    @Operation(summary = "챌린지 투두 완료 처리", description = "특정 챌린지 투두를 완료 상태로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 투두 완료 성공")
    @PatchMapping("/{challengeId}/todos")
    public ChallengeTodoResponse completeChallengeTodo(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        todoService.completeChallenge(challengeId, member, LocalDate.now());
        var todoResult = todoService.getChallengeTodoByChallenge(challengeId, member, LocalDate.now());
        return todoMapper.toResponse(todoResult);
    }

    @Operation(summary = "챌린지 투두 완료 취소", description = "특정 챌린지 투두의 완료를 취소합니다.")
    @ApiResponse(responseCode = "204", description = "챌린지 투두 완료 취소 성공")
    @DeleteMapping("/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelChallengeTodo(
            @PathVariable Long todoId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        todoService.cancelCompleteChallenge(todoId, member);
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
