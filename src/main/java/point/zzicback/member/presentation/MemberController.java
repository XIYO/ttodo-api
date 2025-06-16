package point.zzicback.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.application.dto.result.MemberResult;
import point.zzicback.member.presentation.dto.request.UpdateMemberRequest;
import point.zzicback.member.presentation.dto.response.MemberResponse;
import point.zzicback.member.presentation.mapper.MemberPresentationMapper;

import java.util.UUID;

/**
 * 회원 관련 Presentation 레이어 API 컨트롤러
 */
@Tag(name = "멤버", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberPresentationMapper mapper;

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이징 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 목록 조회 성공")
    @GetMapping
    public Page<MemberResponse> getMembers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return memberService.getMembers(pageable)
                .map(mapper::toResponse);
    }

    @Operation(summary = "회원 상세 조회", description = "특정 회원의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
    @GetMapping("/{memberId}")
    public MemberResponse getMember(@PathVariable UUID memberId) {
        MemberResult dto = memberService.getMember(memberId);
        return mapper.toResponse(dto);
    }

    @Operation(summary = "회원 정보 수정", description = "회원 닉네임을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "회원 정보 수정 성공")
    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않습니다.")
    @PatchMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMember(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberRequest request) {
        UpdateMemberCommand command = mapper.toCommand(memberId, request);
        memberService.updateMember(command);
    }
}