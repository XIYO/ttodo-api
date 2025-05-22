package point.zzicback.member.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import point.zzicback.common.utill.CookieUtil;
import point.zzicback.common.utill.JwtUtil;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.AuthenticatedMember;
import point.zzicback.member.domain.dto.request.SignInRequest;
import point.zzicback.member.domain.dto.request.SignUpRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    @PostMapping("/sign-up")
    public void signUpJson(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request.toCommand());
    }

    @PostMapping("/sign-in")
    public void signInJson(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
        AuthenticatedMember authenticatedMember = memberService.signIn(request.toDto());
        String jwtToken = jwtUtil.generateJwtToken(authenticatedMember.id(), authenticatedMember.email(), authenticatedMember.nickname());
        Cookie jwtCookie = cookieUtil.createJwtCookie(jwtToken);
        response.addCookie(jwtCookie);
    }

    @PostMapping("/sign-out")
    public void signOut(HttpServletResponse response) {
        Cookie expiredCookie = cookieUtil.createJwtCookie("");
        cookieUtil.zeroAge(expiredCookie);
        response.addCookie(expiredCookie);
    }
}
