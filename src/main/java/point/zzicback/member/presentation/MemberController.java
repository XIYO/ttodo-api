package point.zzicback.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.query.MemberQuery;
import point.zzicback.member.presentation.dto.MemberMeResponse;
import point.zzicback.member.presentation.mapper.MemberPresentationMapper;

import java.util.UUID;

@Tag(name = "회원", description = "회원 관련 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberPresentationMapper memberPresentationMapper;

    @Operation(summary = "회원 정보 조회", description = "현재 로그인한 회원의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공")
    @GetMapping("/{memberId}/me")
    @ResponseStatus(HttpStatus.OK)
    public MemberMeResponse getMemberMe(@PathVariable UUID memberId) {
        return memberPresentationMapper.toResponse(
                memberService.getMemberMe(MemberQuery.of(memberId))
        );
    }
}
