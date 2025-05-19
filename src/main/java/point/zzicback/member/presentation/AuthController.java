package point.zzicback.member.presentation;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.dto.request.SignUpRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/sign-up")
    public void signUpJson(
            @Valid @RequestBody SignUpRequest signUpRequest,
            HttpServletResponse response
    ) {
        memberService.signUp(signUpRequest);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }
}
