package point.zzicback.todo.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
import point.zzicback.todo.application.RepeatTodoService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/repeat-todos")
@RequiredArgsConstructor
@Tag(name = "반복 투두", description = "반복 투두 관리 API")
public class RepeatTodoController {
    
    private final RepeatTodoService repeatTodoService;
    private final MemberService memberService;
    
    @Operation(summary = "반복 투두 완료 처리", description = "특정 반복 투두를 완료 상태로 변경합니다.")
    @ApiResponse(responseCode = "200", description = "반복 투두 완료 성공")
    @PostMapping("/{originalTodoId}/complete")
    public ResponseEntity<Void> completeRepeatTodo(
            @PathVariable Long originalTodoId,
            @AuthenticationPrincipal MemberPrincipal principal) {
        Member member = memberService.findVerifiedMember(principal.id());
        repeatTodoService.completeRepeatTodo(member.getId(), originalTodoId, LocalDate.now());
        return ResponseEntity.ok().build();
    }
}
