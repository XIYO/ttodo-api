package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeService;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.ChallengeDetailResult;
import point.zzicback.challenge.application.dto.result.ChallengeResult;
import point.zzicback.challenge.application.dto.result.ChallengeListResult;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeVisibility;
import point.zzicback.challenge.presentation.dto.request.CreateChallengeRequest;
import point.zzicback.challenge.presentation.dto.request.UpdateChallengeRequest;
import point.zzicback.challenge.presentation.dto.response.ChallengeDetailResponse;
import point.zzicback.challenge.presentation.dto.response.ChallengeResponse;
import point.zzicback.challenge.presentation.dto.response.InviteLinkResponse;
import point.zzicback.challenge.presentation.mapper.ChallengePresentationMapper;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.util.Arrays;
import java.util.List;

/**
 * 챌린지 기본 CRUD API 컨트롤러
 */
@Tag(name = "챌린지 기본 관리", description = "챌린지 생성, 조회, 수정, 삭제 등 기본 CRUD 기능과 초대 링크 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final MemberService memberService;
    private final ChallengePresentationMapper challengePresentationMapper;

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
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(
            @Valid CreateChallengeRequest request,
            @AuthenticationPrincipal MemberPrincipal principal) {
        
        validatePaginationParams(0, 20, "id,desc"); // 기본 검증
        
        Member member = memberService.findVerifiedMember(principal.id());
        CreateChallengeCommand command = challengePresentationMapper.toCommand(request, member.getId());
        Long challengeId = challengeService.createChallenge(command);
        ChallengeResult result = challengeService.getChallengeDetailForPublic(challengeId);
        return challengePresentationMapper.toChallengeResponse(result);
    }

    @Operation(summary = "챌린지 목록 조회", description = "공개 챌린지 목록을 페이징 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 목록 조회 성공")
    @GetMapping
    public Page<ChallengeResponse> getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        
        validatePaginationParams(page, size, sort);
        
        Pageable pageable = createPageable(page, size, sort);
        Page<ChallengeListResult> challengePage = challengeService.searchChallengesWithFilter(null, null, null, null, pageable);
        return challengePage.map(challengePresentationMapper::toResponse);
    }

    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @GetMapping("/{challengeId}")
    public ChallengeDetailResponse getChallenge(@PathVariable Long challengeId) {
        ChallengeResult result = challengeService.getChallengeDetailForPublic(challengeId);
        return challengePresentationMapper.toResponse(result);
    }

    @Operation(summary = "챌린지 수정", description = "챌린지 정보를 수정합니다. (관리자만)")
    @ApiResponse(responseCode = "204", description = "챌린지 수정 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @PatchMapping(value = "/{challengeId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@challengeService.isOwner(#challengeId, authentication.principal.id)")
    public void updateChallenge(
            @PathVariable Long challengeId,
            @Valid UpdateChallengeRequest request,
            @AuthenticationPrincipal MemberPrincipal principal) {
        UpdateChallengeCommand command = challengePresentationMapper.toCommand(request);
        challengeService.partialUpdateChallenge(challengeId, command);
    }

    @Operation(summary = "챌린지 삭제", description = "챌린지를 삭제합니다. (관리자만)")
    @ApiResponse(responseCode = "204", description = "챌린지 삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @DeleteMapping("/{challengeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@challengeService.isOwner(#challengeId, authentication.principal.id)")
    public void deleteChallenge(@PathVariable Long challengeId) {
        challengeService.deleteChallenge(challengeId);
    }

    @Operation(summary = "초대 링크 조회", description = "챌린지의 초대 링크를 조회합니다. (관리자만)")
    @ApiResponse(responseCode = "200", description = "초대 링크 조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @GetMapping("/{challengeId}/invite-link")
    @PreAuthorize("@challengeService.isOwner(#challengeId, authentication.principal.id)")
    public InviteLinkResponse getInviteLink(@PathVariable Long challengeId) {
        Challenge challenge = challengeService.getChallenge(challengeId);
        return new InviteLinkResponse(
            challenge.getInviteCode(),
            "https://zzic.com/challenges/invite/" + challenge.getInviteCode()
        );
    }

    private void validatePaginationParams(int page, int size, String sort) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다");
        }
        if (size > 100) {
            throw new IllegalArgumentException("페이지 크기는 100 이하여야 합니다");
        }
        
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        List<String> allowedSortFields = Arrays.asList("id", "title", "createdAt", "updatedAt", "startDate", "endDate", "participantCount");
        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("허용되지 않은 정렬 필드입니다: " + sortBy);
        }
    }

    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}